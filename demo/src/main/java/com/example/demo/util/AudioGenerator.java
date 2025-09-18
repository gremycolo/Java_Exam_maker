package com.example.demo.util;

import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;

@Component
public class AudioGenerator {

    private static final String VOICEVOX_URL = "http://127.0.0.1:50021";

    public String textToBase64Audio(String text, int speakerId) throws Exception {
        RestTemplate restTemplate = new RestTemplate();

        // 1) audio_query
        String audioQueryUrl = VOICEVOX_URL + "/audio_query?text=" + text + "&speaker=" + speakerId;
        ResponseEntity<String> queryResponse = restTemplate.postForEntity(audioQueryUrl, null, String.class);

        if (!queryResponse.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("VoiceVox audio_query failed: " + queryResponse);
        }

        String queryJson = queryResponse.getBody();

        // 2) synthesis
        String synthesisUrl = VOICEVOX_URL + "/synthesis?speaker=" + speakerId;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> synthesisRequest = new HttpEntity<>(queryJson, headers);
        ResponseEntity<byte[]> synthesisResponse = restTemplate.postForEntity(synthesisUrl, synthesisRequest, byte[].class);

        if (!synthesisResponse.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("VoiceVox synthesis failed: " + synthesisResponse);
        }

        byte[] wavData = synthesisResponse.getBody();

        // 3) return as Base64 (React will decode & play)
        return Base64.getEncoder().encodeToString(wavData);
    }
}

