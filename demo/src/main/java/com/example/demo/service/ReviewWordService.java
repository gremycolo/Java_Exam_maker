package com.example.demo.service;

//import com.example.demo.entity.QuestionAndChoices;
//import com.example.demo.entity.ReviewWord;
import com.example.demo.entity.*;
import com.example.demo.repository.QuizResultRepository;
import com.example.demo.repository.ReviewWordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class ReviewWordService {

    @Autowired
    private ReviewWordRepository reviewWordRepository;

    @Autowired
    private QuizResultRepository quizResultRepository;

    int seed = 27052025;//69422;
    int choiceShuffleSeed = seed;
    //Random random = new Random(seed);

    public List<QuestionAndChoices> reviewquestionsForTheDay(){
        Random random = new Random(seed);

        List<QuestionAndChoices> qcList = new ArrayList<>();
        for(int i = 0; i < 100; i++){
            int id = random.nextInt(reviewWordRepository.findMaxInsertOrder()) + 1;
            QuestionAndChoices qc = new QuestionAndChoices();
            Optional<ReviewWord> reviewWordOptional = reviewWordRepository.findById((long) id);
            qc.setJpWriting(reviewWordOptional
                    .map(ReviewWord::getJpWriting) // Get jpWriting if Vocabulary exists
                    .orElse(null));
            qc.setNumber(i+1);
            qc.setId(Math.toIntExact(reviewWordOptional
                    .map(ReviewWord::getId) // Get id if Vocabulary exists
                    .orElse(null)));


            List<String> choices = new ArrayList<>();
            for(int j = 0; j < 3; j++){
                Optional<ReviewWord> optTAReviewWord = reviewWordRepository.findById(Long.valueOf(idRandDifferentiator(id, random)));
                //getVocabularyById(idRandDifferentiator(id)); //makes sure it is not the same as questionID
                choices.add(optTAReviewWord
                        .map(ReviewWord::getMeaning) // REMINDER USE AI TO ASSESS IF SAME MEANING
                        .orElse(null));
            }
            choices.add(reviewWordOptional
                    .map(ReviewWord::getMeaning)
                    .orElse(null));


            //choiceShuffleSeed = (7*choiceShuffleSeed+500)%seed;
            Collections.shuffle(choices, random);


            qc.setA(choices.get(0));// getVocabularyById(id);
            qc.setB(choices.get(1));
            qc.setC(choices.get(2));
            qc.setD(choices.get(3));
            qcList.add(qc);
        }

        return qcList;/////////////////////////////RETURN!
    }


    private int idRandDifferentiator(int srcId, Random random){
        int wrongId = random.nextInt(reviewWordRepository.findMaxInsertOrder()) + 1;
        while(srcId == wrongId){
            wrongId = random.nextInt(reviewWordRepository.findMaxInsertOrder()) + 1;
        }
        return wrongId;
    }


    public String processReviewQuizSubmission(QuizSubmission submission) {
        String name = submission.getName();
        List<Answer> answers = submission.getAnswers();
        //int totalQuestions = answers.size();
        int correctAnswers = 0;
        List<String> mistakes = new ArrayList<>();

        int k = 1;
        for (Answer answer : answers) {
            int questionId = answer.getId();
            answer.setNumber(k);
            String userAnswer = answer.getAnswer();
            Optional<ReviewWord> reviewWordOptional = reviewWordRepository.findById((long) questionId);
            String correctAnswer = reviewWordOptional
                    .map(ReviewWord::getMeaning)
                    .orElse(null);

            if (correctAnswer != null && correctAnswer.equals(userAnswer)) {
                correctAnswers++;
            } else if (correctAnswer != null) {
                mistakes.add(k + ""); // Add question number to mistakes
            }
            k++;
            System.out.println(correctAnswer + "-----" +userAnswer);
        }

        int score = correctAnswers; // Percentage score
        QuizResult quizResult = new QuizResult(name, score, mistakes, LocalDateTime.now());
        quizResultRepository.save(quizResult);

        return "Quiz submission processed for " + name + ". Score: " + score + "%, Mistakes: " +
                mistakes.stream().collect(Collectors.joining(", "));
    }


}
