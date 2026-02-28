from Modules.groq_client import get_response
import os

# Loads the directed conversation, checks if the qdrant collection for it is up to date, and if not, embeds new messages
def handle_context(data: dict):
    conv_id = data.get("conv_id")
    context = data.get("context")

# Will look to see if the conv_id exists as a qdrant collection, if not it will create one and embed the prompt, and generate and send a conversation title.
# If it does exist, it will embed the prompt to the specified conversation collection, and then generate a streamed response, sending the response back to the client as it is generated using the chunk data format
# ...lowkey might be good to have a folder of json files with the conv_id as the filename to store conversation history for retrieval for the sake of sending it to the model as context

LIMIT = 10 # number of previous messages to send as context
DEFAULT_USER = "user"
def handle_prompt(data: dict):
    print("Handling prompt: ", data)
    data = data.get("data")
    conv_id = data.get("conv_id")
    prompt = data.get("prompt")
    user = DEFAULT_USER # We're only doing one user for now, but it's as easy as adding a user field later on
    try:
        messages = os.path.join(f"users/{user}/conversations/{conv_id}/", f"context.json")
    except Exception as e:
        print(f"Error loading conversation history: {e}")
    messages = []
    messages.append({"role": "user", "content": prompt})
    messages = messages[-LIMIT:]
    print("Messages being sent to model: ", messages)
    response = get_response(messages)
    return response

    
    




