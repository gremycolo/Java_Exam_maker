import requests
import numpy as np

def get_embedding(text):
    url = "http://localhost:11434/api/embeddings"
    payload = {"model": "nomic-embed-text", "prompt": text}
    response = requests.post(url, json=payload)
    response.raise_for_status()  # raises error if request fails
    embedding = response.json()["embedding"]
    return np.array(embedding)

def cosine_similarity(vec1, vec2):
    return np.dot(vec1, vec2) / (np.linalg.norm(vec1) * np.linalg.norm(vec2))

# Example usage
#also,as I thought,still,in spite of,absolutely,of course
# text1 = "of course"
# text2 = "as expected"
text1 = "2. to poke"
text2 = "to stab"
vec1 = get_embedding(text1)
vec2 = get_embedding(text2)

cos_sim = cosine_similarity(vec1, vec2)
print(f"Cosine similarity between '{text1}' and '{text2}': {round(cos_sim, 4)}")
