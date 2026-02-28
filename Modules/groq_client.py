from groq import Groq
import os

MODEL = "openai/gpt-oss-20b"

groq_api_key = os.getenv("GROQ_API_KEY")
if not groq_api_key:
    raise ValueError("GROQ_API_KEY environment variable is not set.")
1
client  = Groq(api_key=groq_api_key)

def get_response(messages):
    response = client.chat.completions.create(
        messages=messages,
        model=MODEL,
        # stream=True
    )
    return response.choices[0].message.content, MODEL

if __name__ == "__main__":
    messages = [
        {"role": "system", "content": "You are a helpful assistant."},
        {"role": "user", "content": "What is the capital of France?"}
    ]
    response = get_response(messages)
    print(response)