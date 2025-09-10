package com.example.demo.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.List;

@Converter
public class EmbeddingConverter implements AttributeConverter<List<List<Float>>, String> {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<List<Float>> attribute) {
        try {
            return mapper.writeValueAsString(attribute);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert embedding to JSON", e);
        }
    }

    @Override
    public List<List<Float>> convertToEntityAttribute(String dbData) {
        try {
            return mapper.readValue(dbData, new TypeReference<List<List<Float>>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert JSON to embedding", e);
        }
    }
}
