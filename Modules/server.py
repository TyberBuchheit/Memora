from fastapi import FastAPI, Request
# from sentence_transformers import SentenceTransformer
from qdrant_client import QdrantClient
from contextlib import asynccontextmanager
from qdrant_client.models import Distance, VectorParams, PointStruct
import cohere

MODEL_NAME = "embed-english-light-v3.0"

import os
api_key = os.environ.get("CO_API_KEY")
co = cohere.ClientV2(api_key=api_key)

app = FastAPI()

DEFAULT_USER = "user" # For now, we're only doing one user, but it's as easy as adding a user field later on

@asynccontextmanager
async def lifespan(app: FastAPI):
    app.state.qdrant = QdrantClient(path="users")
    yield

@app.post("/embed")
async def embed(request: Request):
    data = await request.json()
    text = data.get("text")
    if not text:
        return {"error": "No text provided"}
    embedding = co.embed(texts=[text], model=MODEL_NAME, input_type="search_document").embeddings.float[0]
    return {"embedding": embedding}

@app.post("/similarity_search")
async def similarity_search(request: Request):
    pass # <- will be dun l8r | I am not looking forward to the writeup of the algorithm 
