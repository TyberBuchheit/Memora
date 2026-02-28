from fastapi import FastAPI, Request
from sentence_transformers import SentenceTransformer
from qdrant_client import QdrantClient
from contextlib import asynccontextmanager
from qdrant_client.models import Distance, VectorParams, PointStruct

MODEL_NAME = "BAAI/bge-small-en-v1.5"

app = FastAPI("embedding_server")

@asynccontextmanager
async def lifespan(app: FastAPI):
    app.state.embedder = SentenceTransformer(MODEL_NAME, cache_folder="models/")
    app.state.qdrant = QdrantClient(path="users/")
    yield

@app.post("/embed")
async def embed(request: Request):
    data = await request.json()
    text = data.get("text")
    if not text:
        return {"error": "No text provided"}
    
    embedding = app.state.embedder.encode(text).tolist()
    return {"embedding": embedding}

@app.post("/similarity_search")
async def similarity_search(request: Request):
    pass
    