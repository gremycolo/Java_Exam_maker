// EmbeddingService.java
package com.example.demo.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@Service
public class EmbeddingService {

    private static final String OLLAMA_URL = "http://localhost:11434/api/embeddings";
    private static final String MODEL_NAME = "nomic-embed-text";
    private final ObjectMapper mapper = new ObjectMapper();

    // Call Ollama and get embedding as List<Float>
    public List<Float> getEmbedding(String text) throws Exception {
        URL url = new URL(OLLAMA_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        String payload = String.format("{\"model\":\"%s\",\"prompt\":\"%s\"}", MODEL_NAME, text);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(payload.getBytes());
        }

        Scanner scanner = new Scanner(conn.getInputStream()).useDelimiter("\\A");
        String response = scanner.hasNext() ? scanner.next() : "";
        scanner.close();

        JsonNode json = mapper.readTree(response);
        JsonNode embeddingNode = json.get("embedding");

        List<Float> embedding = new ArrayList<>();
        for (JsonNode n : embeddingNode) {
            embedding.add(n.floatValue());
        }
        return embedding;
    }

    // Cosine similarity between two embeddings
    public float cosineSimilarity(List<Float> vecA, List<Float> vecB) {
        if (vecA.size() != vecB.size()) throw new IllegalArgumentException("Vectors must be same length");

        float dot = 0f;
        float magA = 0f;
        float magB = 0f;

        for (int i = 0; i < vecA.size(); i++) {
            dot += vecA.get(i) * vecB.get(i);
            magA += vecA.get(i) * vecA.get(i);
            magB += vecB.get(i) * vecB.get(i);
        }

        return dot / ((float) (Math.sqrt(magA) * Math.sqrt(magB)));
    }
}
