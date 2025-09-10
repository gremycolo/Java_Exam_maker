import pyodbc
import requests
import json

# === DB connection ===
conn_str = (
    "DRIVER={ODBC Driver 17 for SQL Server};"
    "SERVER=localhost;"  # adjust if SQL Server is remote or named instance
    "DATABASE=JapaneseVocabularyDB;"
    "UID=springToDBLogin;"
    "PWD=Estoista1;"
)
conn = pyodbc.connect(conn_str)
cursor = conn.cursor()

# === Ollama endpoint ===
OLLAMA_URL = "http://localhost:11434/api/embeddings"
MODEL_NAME = "nomic-embed-text"

def get_embedding(text: str):
    payload = {
        "model": MODEL_NAME,
        "prompt": text
    }
    response = requests.post(OLLAMA_URL, json=payload)
    response.raise_for_status()
    data = response.json()

    if "embedding" not in data:
        raise RuntimeError(f"Unexpected response: {data}")

    return data["embedding"]

# === Fetch vocab entries without embeddings ===
cursor.execute("SELECT InsertOrder, Meaning FROM Vocabulary WHERE MeaningEmbedding IS NULL")
rows = cursor.fetchall()

print(f"Found {len(rows)} vocab entries without embeddings.")

for idx, row in enumerate(rows, start=1):
    insert_order, meaning = row
    if not meaning:
        continue

    try:
        # Split by comma and strip spaces
        words = [w.strip() for w in meaning.split(",") if w.strip()]
        embeddings_list = []

        for w in words:
            embedding = get_embedding(w)
            embeddings_list.append(embedding)

        # Convert the list of embeddings to JSON
        embedding_json = json.dumps(embeddings_list, ensure_ascii=False)

        cursor.execute(
            "UPDATE Vocabulary SET MeaningEmbedding = ? WHERE InsertOrder = ?",
            (embedding_json, insert_order)
        )

        # Commit every 50 rows to avoid huge transaction
        if idx % 50 == 0:
            conn.commit()

        print(f"✅ InsertOrder={insert_order}, Meaning='{meaning[:20]}...', embeddings={len(embeddings_list)}")

    except Exception as e:
        print(f"❌ Failed InsertOrder={insert_order}, Error={e}")

# Final commit
conn.commit()
cursor.close()
conn.close()
print("Done embedding all vocab entries.")
