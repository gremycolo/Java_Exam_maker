package com.example.demo.controller;


import com.example.demo.entity.QuestionAndChoices;
import com.example.demo.entity.QuizResultResponse;
import com.example.demo.entity.QuizSubmission;
import com.example.demo.entity.Vocabulary;
import com.example.demo.service.ReviewWordService;
import com.example.demo.service.VocabularyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vocabulary")
@CrossOrigin//(origins = "*")
public class VocabularyController {

    @Autowired
    private VocabularyService vocabularyService;

    @Autowired
    private ReviewWordService reviewWordService;

    @GetMapping
    public List<Vocabulary> getAllVocabulary() {
        return vocabularyService.getAllVocabulary();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Vocabulary> getVocabularyById(@PathVariable int id) {
        return vocabularyService.getVocabularyById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }


    //@CrossOrigin
    @GetMapping("/100Words")
    public List<QuestionAndChoices> getReviewQuestionsForTheDay() {
        return vocabularyService.customQuestions();
    }


    //@CrossOrigin
    @PutMapping("/api/submitQuiz/bak")
    public String processReviewQuizSubmission(@RequestBody QuizSubmission submission) {
        return reviewWordService.processReviewQuizSubmission(submission);
    }

    @PutMapping("/api/submitQuiz")
    public QuizResultResponse processQuizSubmission(@RequestBody QuizSubmission submission) throws Exception {
        return vocabularyService.processQuizSubmission(submission, true);
    }

    @PutMapping("/api/submitIdentificationQuiz")
    public QuizResultResponse processIdentificationQuizSubmission(@RequestBody QuizSubmission submission) throws Exception {
        return vocabularyService.processQuizSubmission(submission, false);
    }

    @PostMapping
    public Vocabulary createVocabulary(@RequestBody Vocabulary vocabulary) {
        return vocabularyService.createVocabulary(vocabulary);
    }

}