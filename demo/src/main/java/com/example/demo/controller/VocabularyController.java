package com.example.demo.controller;


import com.example.demo.entity.QuestionAndChoices;
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

//    @GetMapping("/100Words/bak")
//    public List<QuestionAndChoices> get100Words(){
//        return vocabularyService.questionsForTheDay();
//    }


//    //@CrossOrigin
//    @GetMapping("/100Words/finishedWords")
//    public List<QuestionAndChoices> getReviewQuestionsForTheDayFinishedWords() {
//        return reviewWordService.reviewquestionsForTheDay();
//    }

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
    public String processQuizSubmission(@RequestBody QuizSubmission submission) {
        return vocabularyService.processQuizSubmission(submission);
    }

    @PostMapping
    public Vocabulary createVocabulary(@RequestBody Vocabulary vocabulary) {
        return vocabularyService.createVocabulary(vocabulary);
    }

//    @PutMapping("/{id}")
//    public ResponseEntity<Vocabulary> updateVocabulary(@PathVariable int id, @RequestBody Vocabulary vocabularyDetails) {
//        try {
//            Vocabulary updatedVocabulary = vocabularyService.updateVocabulary(id, vocabularyDetails);
//            return ResponseEntity.ok(updatedVocabulary);
//        } catch (RuntimeException e) {
//            return ResponseEntity.notFound().build();
//        }
//    }

//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> deleteVocabulary(@PathVariable int id) {
//        try {
//            vocabularyService.deleteVocabulary(id);
//            return ResponseEntity.ok().build();
//        } catch (RuntimeException e) {
//            return ResponseEntity.notFound().build();
//        }
//    }
}