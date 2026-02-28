text_to_embed = "The quick brown fox jumps over the lazy dog."

import requests

response = requests.post("http://localhost:8000/embed", json={"text": text_to_embed})
print(response.json())