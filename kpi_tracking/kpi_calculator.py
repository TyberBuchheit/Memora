import tiktoken

LLM_TOKEN_PRICE = {
    "GPT OSS 20B 128k": [.075, .30],
    "GPT OSS 120B 128k": [.0075, .30],
    "Llama 3.3 70B Versatile 128k": [.11, .34],
    "Qwen3 32B 131k": [.29, .59]
}

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

        content = message.get("content", "")
        if content:
            input_tokens += len(encoding.encode(content))

    last_message = messages[-1]

    if last_message.get("role") == "assistant":
        content = last_message.get("content", "")
        if content:
            output_tokens += len(encoding.encode(content))
    

    return [input_tokens, output_tokens]

def compare_costs(w_tcount, n_tcount, model_n):
    """
    Compares non-Memora token-counts (n_tcount) and Memora token-counts (w_tcount)
    and demonstrates the difference in token consumption and cost,
    returns list -> [Memora Cost, !Memora Cost]
    """

    LLM_TOKEN_PRICE = {
    "GPT OSS 20B 128k": [.075, .30],
    "GPT OSS 120B 128k": [.0075, .30],
    "Llama 3.3 70B Versatile 128k": [.11, .34],
    "Qwen3 32B 131k": [.29, .59]
    }


    w_cost = ((w_tcount[0] / 1000000) * LLM_TOKEN_PRICE[model_n][0]) + ((w_tcount[1] / 1000000) * LLM_TOKEN_PRICE[model_n][1]) # Cost using Memora
    n_cost = ((n_tcount[0] / 1000000) * LLM_TOKEN_PRICE[model_n][0]) + ((n_tcount[1] / 1000000) * LLM_TOKEN_PRICE[model_n][1]) # Cost !using Memora
    
    print("Non-Memora - Memora: ", n_cost - w_cost)

    return [w_cost, n_cost]