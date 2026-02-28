from Modules.groq_client import get_response
import requests
import logging
import tiktoken
import json
import sys
import os

EMBED_SERVER_URL = "http://localhost:8000"
LIMIT = 10 # number of previous messages to send as context
DEFAULT_USER = "user"
LOGFILE = "memora_logging.log"

#Init Logger
logging.basicConfig(level=logging.INFO, filename=LOGFILE, filemode="a")
logger = logging.getLogger("memora_logging")

# Loads the directed conversation, checks if the qdrant collection for it is up to date, and if not, embeds new messages
def handle_context(data: dict):
    conv_id = data.get("conv_id")
    context = data.get("context")

# Will look to see if the conv_id exists as a qdrant collection, if not it will create one and embed the prompt, and generate and send a conversation title.
# If it does exist, it will embed the prompt to the specified conversation collection, and then generate a streamed response, sending the response back to the client as it is generated using the chunk data format

def token_count(messages, model) -> list:
    """
    Calculates the tokens used in a conversation based on these agents:
    GPT OSS 20B 128k
    GPT OSS 120B 128k
    Qwen3 32B 131k
    Llama 3.3 70B Versatile 128k
    then returns the 'in' and 'out' tokens in list format as
    list[in, out]
    """

    try:
        encoding = tiktoken.encoding_for_model(model)
    except Exception:
        encoding = tiktoken.get_encoding("cl100k_base")

    if model == "GPT OSS 20B 128k" or model == "GPT OSS 120B 128k" or model == "Llama 3.3 70B Versatile 128k":
        tokens_per_message = 3
    if model == "Qwen3 32B 131k":
        tokens_per_message = 4
    else:
        tokens_per_message = 3 # Our system


    input_tokens = 0
    output_tokens = 0

    for message in messages[:-1]: # Excluding last message

        input_tokens += tokens_per_message

        try:
            content = message.get("content", "")
        except:
            print("(Exit) Message: ", messages)
            quit()
        if content:
            if message.get('role') == ("user" or "system"):
                input_tokens += len(encoding.encode(content))
            elif message.get('role') == "assistant":
                output_tokens += len(encoding.encode(content))

    # last_message = messages[-1]

    # if last_message.get("role") == "assistant":
    #     content = last_message.get("content", "")
    #     if content:
    #         output_tokens += len(encoding.encode(content))
    

    return [input_tokens, output_tokens]


def compare_costs(w_tcount, n_tcount, model_n):
    """
    Compares non-Memora token-counts (n_tcount) and Memora token-counts (w_tcount)
    and demonstrates the difference in token consumption and cost,
    returns list -> [Memora Cost, !Memora Cost]
    """

    LLM_TOKEN_PRICE = {
        "openai/gpt-oss-20b": [0.075, 0.30],
        "openai/gpt-oss-120b": [0.0075, 0.30],
        "meta-llama/Llama-3.3-70B-Versatile": [0.11, 0.34],
        "qwen/Qwen3-32B": [0.29, 0.59]
    }

    w_input = w_tcount[0]
    w_output = w_tcount[1]
    w_input_cost = (w_input/1000000) * (LLM_TOKEN_PRICE[model_n][0])
    w_output_cost = (w_output/1000000) * (LLM_TOKEN_PRICE[model_n][1])

    n_input = n_tcount[0]
    n_output = n_tcount[1]
    n_input_cost = (n_input/1000000) * (LLM_TOKEN_PRICE[model_n][0])
    n_output_cost = (n_output/1000000) * (LLM_TOKEN_PRICE[model_n][1])

    print(f"w_input: {w_input_cost}, w_output: {w_output_cost}, n_input: {n_input_cost}, n_output: {n_output_cost}")

    ratio = (w_input_cost + w_output_cost) / (n_input_cost + n_output_cost)
    # print(f"Ratio: {ratio}")
    
    w_cost = ((w_tcount[0] / 1000000) * LLM_TOKEN_PRICE[model_n][0]) + ((w_tcount[1] / 1000000) * LLM_TOKEN_PRICE[model_n][1]) # Cost using Memora
    n_cost = ((n_tcount[0] / 1000000) * LLM_TOKEN_PRICE[model_n][0]) + ((n_tcount[1] / 1000000) * LLM_TOKEN_PRICE[model_n][1]) # Cost !using Memora
    
    token_efficiency = "Memora Token Efficiency by %: ", (((w_input + w_output)) / (n_input + n_output)) * 100
    price_efficiency = "Memora Cost Difference by %: ", (w_cost / n_cost) * 100
    print(token_efficiency, ", ", price_efficiency)

    inputs_tkn_ratios = ((w_input / n_input) * 100)
    outputs_tkn_ratios = ((w_output / n_output) * 100)
    
    inputs_cost_ratios = ((w_input_cost / n_input_cost) * 100)
    outputs_cost_ratios = ((w_output_cost / n_output_cost) * 100)

    print("Input cost ratio: ", inputs_cost_ratios)
    print("Outpus costs ratio: ", outputs_cost_ratios)


    logger.log(20, (f"Input Token Use Ratio: {inputs_tkn_ratios}, Output Token Use Ratio: {outputs_tkn_ratios} | Input Cost Ratio: {inputs_cost_ratios}, Output Cost Ratio: {outputs_cost_ratios}"))


    return ("Costs: %0.6f vs %0.6f", w_cost, n_cost)


def handle_prompt(data: dict):
    print("Handling prompt: ", data)
    data = data.get("data")
    conv_id = data.get("conv_id")
    prompt = data.get("prompt")
    user = DEFAULT_USER # We're only doing one user for now, but it's as easy as adding a user field later on

    context_path = os.path.join("memora", "Users", user, "Conversations", conv_id, "context.json")
    try:
        with open(context_path, "r", encoding="utf-8", errors="replace") as f:
            raw = json.load(f)
        history = raw.get("conversation", []) #! history is chat log WITHOUT Memora
    except FileNotFoundError as e:
        print(f"Error loading conversation history: {e}")
        history = []

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
    messages = [] #! Chat log with Memora
    if memory_summary:
        messages.append({"role": "system", "content": f"You are a helpful assistant.\n\nMEMORY:\n{memory_summary}"})
    else:
        messages.append({"role": "system", "content": "You are a helpful assistant."})

    true_limit = LIMIT - 2
    
    # print(f"HISTORY: {history}") #! TESTING

    messages += history[-true_limit:]
    messages.append({"role": "user", "content": prompt})
    # print("Messages: ", messages) #!TESTING

   
    response, model = get_response(messages)

    # Logging token use and cost benefit analysis
    w_token_result = token_count(messages, model)
    n_token_result = token_count(history, model)
    token_costs = compare_costs(w_token_result, n_token_result, model)


    return response
    
    




