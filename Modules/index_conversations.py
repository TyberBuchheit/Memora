"""
Run this script to embed all conversations and upsert them into Qdrant.
Requires the embedding server (server.py) to be running at EMBED_SERVER_URL.
"""

import os
import sys
import json
import requests
from qdrant_client import QdrantClient
from qdrant_client.models import Distance, VectorParams, PointStruct

EMBED_SERVER_URL = "http://localhost:8000/embed"
VECTOR_SIZE = 384  # embed-english-light-v3.0 output dim
DEFAULT_USER = "user"
BAR_WIDTH = 40

# ── Terminal helpers ───────────────────────────────────────────────────────────

def move_up(n):
    sys.stdout.write(f"\033[{n}A")

def clear_line():
    sys.stdout.write("\033[2K\r")

def bar(filled, total, width=BAR_WIDTH):
    if total == 0:
        pct = 0
    else:
        pct = filled / total
    done = int(width * pct)
    return f"[{'█' * done}{'░' * (width - done)}] {pct*100:5.1f}%"

def render(conv_idx, total_convs, msg_idx, total_msgs):
    """Re-render all 4 display lines in-place."""
    total_msgs_done = conv_idx * 1  # placeholder — actual total tracked outside
    overall_msgs_done = sum(_msg_counts[:conv_idx]) + msg_idx

    move_up(4)

    clear_line()
    sys.stdout.write(f"Overall   {bar(overall_msgs_done, _total_msgs)}\n")

    clear_line()
    sys.stdout.write(f"Convo     {bar(msg_idx, total_msgs)}\n")

    clear_line()
    sys.stdout.write(f"Conversation : {conv_idx + 1} / {total_convs}\n")

    clear_line()
    sys.stdout.write(f"Message      : {msg_idx} / {total_msgs}\n")

    sys.stdout.flush()

# Globals used by render()
_msg_counts = []
_total_msgs = 1

# ── Embedding ─────────────────────────────────────────────────────────────────

def get_embedding(text: str) -> list[float]:
    response = requests.post(EMBED_SERVER_URL, json={"text": text})
    response.raise_for_status()
    return response.json()["embedding"]

# ── Main ──────────────────────────────────────────────────────────────────────

def index_conversations():
    global _msg_counts, _total_msgs

    conversations_path = os.path.join("memora", "Users", DEFAULT_USER, "Conversations")
    qdrant = QdrantClient(path=os.path.join("memora", "Users", DEFAULT_USER))

    # Pre-load all conversation data so we know total message counts upfront
    conversations = []
    for folder in os.listdir(conversations_path):
        conv_dir = os.path.join(conversations_path, folder)
        context_path = os.path.join(conv_dir, "context.json")
        if not os.path.isfile(context_path):
            continue
        with open(context_path, "r", encoding="utf-8-sig") as f:
            data = json.load(f)
        if isinstance(data, list):
            conv_id = folder
            messages = data
        else:
            conv_id = data.get("conversation_id", folder)
            messages = data.get("conversation", [])
        if messages:
            conversations.append((conv_id, messages))

    total_convs = len(conversations)
    _msg_counts = [len(msgs) for _, msgs in conversations]
    _total_msgs = max(sum(_msg_counts), 1)

    # Print the 4 lines that render() will overwrite in-place
    sys.stdout.write(f"Overall   {bar(0, _total_msgs)}\n")
    sys.stdout.write(f"Convo     {bar(0, 1)}\n")
    sys.stdout.write(f"Conversation : 0 / {total_convs}\n")
    sys.stdout.write(f"Message      : 0 / 0\n")
    sys.stdout.flush()

    indexed = []
    errors = []

    for conv_idx, (conv_id, messages) in enumerate(conversations):
        total_msgs = len(messages)

        existing = [c.name for c in qdrant.get_collections().collections]
        if conv_id not in existing:
            qdrant.create_collection(
                collection_name=conv_id,
                vectors_config=VectorParams(size=VECTOR_SIZE, distance=Distance.COSINE),
            )

        points = []
        for msg_idx, msg in enumerate(messages):
            render(conv_idx, total_convs, msg_idx, total_msgs)
            try:
                vector = get_embedding(msg["content"])
            except requests.RequestException as e:
                errors.append({"conv_id": conv_id, "message_index": msg_idx, "error": str(e)})
                continue

            points.append(PointStruct(
                id=msg_idx,
                vector=vector,
                payload={
                    "role": msg["role"],
                    "content": msg["content"],
                    "message_index": msg_idx,
                    "conv_id": conv_id,
                }
            ))

        if points:
            qdrant.upsert(collection_name=conv_id, points=points)

        render(conv_idx + 1, total_convs, total_msgs, total_msgs)
        indexed.append({"conv_id": conv_id, "messages_indexed": len(points)})

    # Final newline after the display block
    sys.stdout.write("\n")
    print(f"Done. {len(indexed)} conversations indexed, {len(errors)} errors.")
    if errors:
        print("Errors:", json.dumps(errors, indent=2))

if __name__ == "__main__":
    index_conversations()
