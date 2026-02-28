from fastapi import FastAPI, Request
from qdrant_client import QdrantClient
from qdrant_client.models import Distance, VectorParams, PointStruct
from contextlib import asynccontextmanager
from groq import Groq
import cohere
import numpy as np
import os
import json

MODEL_NAME = "embed-english-light-v3.0"
SUMMARY_MODEL = "llama-3.3-70b-versatile"
SIMILARITY_THRESHOLD = 0.77  # midpoint of 0.75–0.80
TOP_K = 5
MAX_DEPTH = 3
MAX_CLUSTER = 50

co_api_key = os.environ.get("CO_API_KEY")
co = cohere.ClientV2(api_key=co_api_key)

groq_api_key = os.environ.get("GROQ_API_KEY")
groq = Groq(api_key=groq_api_key)

DEFAULT_USER = "user"

@asynccontextmanager
async def lifespan(app: FastAPI):
    app.state.qdrant = QdrantClient(path=os.path.join("memora", "Users", DEFAULT_USER))
    yield

app = FastAPI(lifespan=lifespan)

def cosine(a, b):
    a, b = np.array(a), np.array(b)
    return float(np.dot(a, b) / (np.linalg.norm(a) * np.linalg.norm(b)))

def embed_text(text: str) -> list[float]:
    return co.embed(texts=[text], model=MODEL_NAME, input_type="search_query").embeddings.float[0]

def grow_cluster(qdrant: QdrantClient, conv_id: str, query_vector: list[float]) -> list[dict]:
    """
    Query-seeded recursive neighborhood retrieval within a single conversation collection.
    Similarity is checked against the parent node vector, not the original query.
    """
    seed_hits = qdrant.search(
        collection_name=conv_id,
        query_vector=query_vector,
        limit=TOP_K,
        with_vectors=True,
    )

    # key: point id, value: scored point
    cluster = {h.id: h for h in seed_hits if h.score >= SIMILARITY_THRESHOLD}
    frontier = dict(cluster)
    visited  = set(cluster.keys())

    depth = 0
    while frontier and depth < MAX_DEPTH and len(cluster) < MAX_CLUSTER:
        next_frontier = {}
        for node in frontier.values():
            neighbors = qdrant.search(
                collection_name=conv_id,
                query_vector=node.vector,
                limit=TOP_K,
                with_vectors=True,
            )
            for nb in neighbors:
                if nb.id in visited:
                    continue
                visited.add(nb.id)
                if cosine(node.vector, nb.vector) >= SIMILARITY_THRESHOLD:
                    next_frontier[nb.id] = nb
                    cluster[nb.id] = nb
        frontier = next_frontier
        depth += 1

    # Sort chronologically by message_index and return as plain dicts
    results = sorted(cluster.values(), key=lambda p: p.payload.get("message_index", 0))
    return [{"message_index": p.payload["message_index"],
             "role": p.payload["role"],
             "content": p.payload["content"],
             "score": p.score} for p in results]

@app.post("/embed")
async def embed(request: Request):
    data = await request.json()
    text = data.get("text")
    if not text:
        return {"error": "No text provided"}
    embedding = embed_text(text)
    return {"embedding": embedding}

@app.post("/similarity_search")
async def similarity_search(request: Request):
    """
    Body: { "query": str, "conv_id": str }
    Returns chronologically sorted cluster of semantically related messages.
    """
    data = await request.json()
    query = data.get("query")
    conv_id = data.get("conv_id")
    if not query or not conv_id:
        return {"error": "query and conv_id are required"}

    qdrant: QdrantClient = request.app.state.qdrant

    existing = [c.name for c in qdrant.get_collections().collections]
    if conv_id not in existing:
        return {"error": f"Collection {conv_id} not found"}

    query_vector = embed_text(query)
    cluster = grow_cluster(qdrant, conv_id, query_vector)

    return {"conv_id": conv_id, "results": cluster}

@app.post("/shorten_convo/{conv_id}")
async def shorten_convo(conv_id: str, request: Request):
    """
    Retrieves the full conversation, runs similarity search seeded by the
    most recent user message, and summarizes the resulting cluster into a
    compact episodic memory string.
    """
    qdrant: QdrantClient = request.app.state.qdrant

    existing = [c.name for c in qdrant.get_collections().collections]
    if conv_id not in existing:
        return {"error": f"Collection {conv_id} not found"}

    data = await request.json()
    query = data.get("query")
    if not query:
        return {"error": "query is required"}

    query_vector = embed_text(query)
    cluster = grow_cluster(qdrant, conv_id, query_vector)

    if not cluster:
        return {"conv_id": conv_id, "summary": ""}

    # Format cluster as a readable transcript for the LLM
    transcript = "\n".join(
        f"{msg['role'].upper()}: {msg['content']}" for msg in cluster
    )

    response = groq.chat.completions.create(
        model=SUMMARY_MODEL,
        messages=[
            {
                "role": "system",
                "content": (
                    "You are a memory summarizer. You will be given a transcript of conversation snippets. "
                    "Write a concise summary in second person — 'you' refers to the assistant. "
                    "Focus only on durable context: tasks, goals, preferences, and decisions. "
                    "Ignore filler, greetings, and small talk. Be brief."
                )
            },
            {
                "role": "user",
                "content": f"Summarize the following conversation snippets:\n\n{transcript}"
            }
        ]
    )

    summary = response.choices[0].message.content
    return {"conv_id": conv_id, "summary": summary, "snippets_used": len(cluster)} 
