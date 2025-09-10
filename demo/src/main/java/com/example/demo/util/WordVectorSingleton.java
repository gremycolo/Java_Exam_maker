package com.example.demo.util;

import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;

public class WordVectorSingleton {
    private static final Logger log = LoggerFactory.getLogger(WordVectorSingleton.class);
    private static Word2Vec word2Vec;

    static {
        try {
            // Set ND4J memory limit via system property
            System.setProperty("org.nd4j.native.maxPhysicalMemory", "12000M"); // 12 GB

            String modelPath = "path/to/GoogleNews-vectors-negative300.bin"; // Adjust path
            log.info("Loading Word2Vec model...");
            word2Vec = WordVectorSerializer.readWord2VecModel(new File(modelPath));
            log.info("Model loaded successfully.");
        } catch (Exception e) {
            log.error("Failed to load Word2Vec model: {}", e.getMessage());
            throw new RuntimeException("Model loading failed", e);
        }
    }

    public static Word2Vec getWord2Vec() {
        return word2Vec;
    }
}