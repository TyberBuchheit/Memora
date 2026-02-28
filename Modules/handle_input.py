from Modules.groq_client import get_response
import requests
import json
import os

EMBED_SERVER_URL = "http://localhost:8000"
LIMIT = 10 # number of previous messages to send as context
DEFAULT_USER = "user"

# Loads the directed conversation, checks if the qdrant collection for it is up to date, and if not, embeds new messages
def handle_context(data: dict):
    conv_id = data.get("conv_id")
    context = data.get("context")

# Will look to see if the conv_id exists as a qdrant collection, if not it will create one and embed the prompt, and generate and send a conversation title.
# If it does exist, it will embed the prompt to the specified conversation collection, and then generate a streamed response, sending the response back to the client as it is generated using the chunk data format

def handle_prompt(data: dict):
    print("Handling prompt: ", data)
    conv_id = data.get("conv_id")
    data = data.get("data")
    prompt = data.get("prompt")
    user = DEFAULT_USER # We're only doing one user for now, but it's as easy as adding a user field later on

    context_path = os.path.join("memora", "Users", user, "Conversations", conv_id, "context.json")
    try:
        with open(context_path, "r", encoding="utf-8", errors="replace") as f:
            raw = json.load(f)
        history = raw if isinstance(raw, list) else []
    except FileNotFoundError as e:
        print(f"Error loading conversation history: {e}")
        history = []

    # the stored conversation entries may come from the Java side, which uses
    # the key "context" instead of "content".  normalize them so the chat
    # client always receives objects with a "content" field.
    def normalize(entry: dict) -> dict:
        if "content" in entry:
            return entry
        if "context" in entry:
            return {"role": entry.get("role"), "content": entry.get("context")}
        # fall back to returning it unmodified; Groq may still complain if it's
        # malformed, but at least we try.
        return entry

    history = [normalize(e) for e in history]

    # Get memory summary from the similarity search + summarization endpoint
    memory_summary = ""
    try:
        resp = requests.post(
            f"{EMBED_SERVER_URL}/shorten_convo/{conv_id}",
            json={"query": prompt}
        )
        resp.raise_for_status()
        memory_summary = resp.json().get("summary", "")
    except requests.RequestException as e:
        print(f"Warning: could not retrieve memory summary: {e}")

    # Build message list: system memory + last LIMIT messages + new prompt
    messages = []
    if memory_summary:
        messages.append({"role": "system", "content": f"You are a helpful assistant.\n\nMEMORY:\n{memory_summary}"})
    else:
        messages.append({"role": "system", "content": "You are a helpful assistant."})

    messages += history[-LIMIT:]
    messages.append({"role": "user", "content": prompt})

    response = get_response(messages)
    return response, conv_id

    
    




