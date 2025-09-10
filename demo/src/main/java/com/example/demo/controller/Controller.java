package com.example.demo.controller;


import com.example.demo.entity.QuestionAndChoices;
import com.example.demo.entity.QuizSubmission;
import com.example.demo.entity.Vocabulary;
import com.example.demo.service.ReviewWordService;
import com.example.demo.service.VocabularyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@CrossOrigin(origins = "*")
public class Controller {

    @GetMapping("/")
    public ResponseEntity<Void> redirectToIndex() {
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create("/index.html"))
                .build();
    }
}