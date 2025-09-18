package com.example.demo.service;

import com.example.demo.entity.*;
import com.example.demo.util.*;
import com.example.demo.repository.QuizResultRepository;
import com.example.demo.repository.VocabularyRepository;
import com.example.demo.util.WordVectorSingleton;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static org.nd4j.linalg.factory.Nd4j.random;

@Service
public class VocabularyService {

    @Autowired
    private VocabularyRepository vocabularyRepository;

    @Autowired
    private QuizResultRepository quizResultRepository;

    @Autowired
    private EmbeddingService embeddingService;

    @Autowired
    private AudioGenerator audioGenerator;


    int seed = 333333333;//69422;
    Double CONFUSION_THRESHOLD = 0.7;
    public String jlptLevel = "N5";
    public boolean allowKatakana = false;
    public boolean noReading = true;        //no furigana
//    public boolean isMultipleChoice = true;
    public boolean choiceReading = true;    //the choices are reading (else writing)
    public String out = "";                 //systemoutput documentation
    public int numberOfItems = 100;
    int[] normalVoices = {
            2, 3, 8, 9, 10, 11, 12, 13, 14,
            16, 20, 21, 23, 27, 28, 29, 42, 43,
            46, 47, 51, 52, 53, 54, 55, 58, 61,
            67, 68, 69, 74, 89, 90, 94, 99, 100,
            102, 107, 108, 109
    }; //18 is ara ara

    @Getter
    private List<QuestionAndChoices> cachedQuestions;

    // Generate once, right after the service is created
    @PostConstruct
    public void init() {
        this.cachedQuestions = customQuestions();
    }

    public List<QuestionAndChoices> regenerateQuestions() {
        this.cachedQuestions = customQuestions();
        return cachedQuestions;
    }


//    public List<Vocabulary> getAllVocabulary() {
//        return vocabularyRepository.findAll();
//    }

    public List<Vocabulary> getAllVocabulary() {
        List<String> levels = Arrays.asList("n1", "n2", "n3", "n4", "n5");
        if (!levels.contains(jlptLevel.toLowerCase()) && allowKatakana){
            out = "No Filters";
            return vocabularyRepository.findAll();
        } else if (levels.contains(jlptLevel.toLowerCase()) && allowKatakana) {
            out = "Filtering by jlpt only";
            return vocabularyRepository.findByJlptLevel(jlptLevel);
        } else if (!levels.contains(jlptLevel.toLowerCase()) && !allowKatakana){
            out = "Filtering by katakana only";
            return vocabularyRepository.findByIsEntirelyKatakana(allowKatakana);
        }
        out = "Filtering by jlpt and katakana";
        return vocabularyRepository.findByJlptLevelAndIsEntirelyKatakana(jlptLevel, allowKatakana);

    }
//    private static final Logger log = (Logger) LoggerFactory.getLogger(VocabularyService.class);
//    //private static final double CONFUSION_THRESHOLD = 0.7;
//    private static final Word2Vec word2Vec = WordVectorSingleton.getWord2Vec();
//
//    @Cacheable(value = "similarityCache", key = "#s1 + '-' + #s2") // Cache key as s1-s2
//    public boolean similarityChecker(String s1, String s2) {
//        try {
//            // Check if meanings exist in the model
//            if (!word2Vec.hasWord(s1) || !word2Vec.hasWord(s2)) {
//                return false;
//            }
//
//            // Get vectors for the meanings
//            double[] vector1 = word2Vec.getWordVector(s1);
//            double[] vector2 = word2Vec.getWordVector(s2);
//
//            // Convert to INDArray for cosine similarity
//            INDArray vec1 = Nd4j.create(vector1);
//            INDArray vec2 = Nd4j.create(vector2);
//
//            // Calculate cosine similarity
//            double similarity = Nd4j.getBlasWrapper().dot(vec1, vec2) / (vec1.norm2().getDouble(0) * vec2.norm2().getDouble(0));
//
//            // Check if they’re too close
//            return similarity < CONFUSION_THRESHOLD;
//
//        } catch (Exception e) {
//            //log.error("Error in similarityChecker for {} and {}: {}", s1, s2, e.getMessage());
//            return false; // Default to true on error to avoid false negatives
//        }
//    }

    

    private int idRandDifferentiator(int srcId, Random random, List<String> choices, java.util.function.Function<Vocabulary, String> extractor){
        Optional<Vocabulary> srcItem = vocabularyRepository.findById(srcId);
        int wrongId;
        String wrongItemVal;
        Optional<Vocabulary> wrongItem;
        do{
            wrongId = random.nextInt(vocabularyRepository.findMaxInsertOrder()) + 1;
            wrongItem = vocabularyRepository.findById(wrongId);
            wrongItemVal = wrongItem.map(extractor).orElse("");
        }
        while(srcId == wrongId ||                                               //if srcId same as wrong id
                //similarityChecker(srcItemMeaning, wrongItemMeaning) ||
                choices.contains(wrongItemVal) ||                               //if in the choices
                !srcItem.map(Vocabulary::getCategory).equals(wrongItem.map(Vocabulary::getCategory)) ||     //if not same CATEGORY as correct answer
                srcItem.map(Vocabulary::getMeaning).equals(wrongItem.map(Vocabulary::getMeaning)));        //if same Meaning as srcId
        return wrongId;
    }

    private void addChoices(List<String> choices, Optional<Vocabulary> reviewWordOptional,
                            int srcId, Random random,
                            java.util.function.Function<Vocabulary, String> extractor) {
        // Add the initial choice (correct answer)
        choices.add(reviewWordOptional
                .map(extractor)
                .orElse(null));

        // Add 3 wrong choices
        for (int j = 0; j < 3; j++) {
            int wrongChoiceId = idRandDifferentiator(srcId, random, choices, extractor);
            Optional<Vocabulary> optWord = vocabularyRepository.findById(wrongChoiceId);
            choices.add(optWord
                    .map(extractor)
                    .orElse(null));
        }
    }

    //Picks items from custom id's in the database then uses it as problems
    public List<QuestionAndChoices> customQuestions() {
        Random random = new Random(seed);
        List<Integer> voice_num_list = new ArrayList<>();
        List<Integer> quizItems = getAllVocabulary().stream()
                .map(Vocabulary::getInsertOrder)
                .collect(Collectors.toList());
        Collections.shuffle(quizItems, random);
        quizItems = quizItems.subList(0, Math.min(numberOfItems, quizItems.size()));



//        // Ensure only existing IDs are picked
//        while (quizItems.size() < numberOfItems) {
//            int id = random.nextInt(vocabularyRepository.findMaxInsertOrder()) + 1;
//            if (!quizItems.contains(id) && vocabularyRepository.existsById(id)) {
//                quizItems.add(id);
//            }
//        }

        List<QuestionAndChoices> qcList = new ArrayList<>();
        List<String> reviewer = new ArrayList<>();

        for (int i = 0; i < quizItems.size(); i++) {
            int currentId = quizItems.get(i);
            Optional<Vocabulary> reviewWordOptional = vocabularyRepository.findById(currentId);

            // Safety check (should always pass because of existsById)
            if (reviewWordOptional.isEmpty()) continue;

            Vocabulary reviewWord = reviewWordOptional.get();

            // Prepare QuestionAndChoices
            QuestionAndChoices qc = new QuestionAndChoices();

//            qc.setJpReading(reviewWord.getJpReading()); //bandaid solution for reading
            String stringAudioJpReading = null;
            try {
                int voice_num = random.nextInt(39) + 1;
//                voice_num_list.add(voice_num);
                stringAudioJpReading = audioGenerator.textToBase64Audio(reviewWord.getJpReading(), normalVoices[random.nextInt(normalVoices.length)]);
//                if(i == 0){
//                    stringAudioJpReading = audioGenerator.textToBase64Audio("今日は私と一緒に公園に行きませんか。", 18);
//                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            qc.setJpReadingBase64(stringAudioJpReading);


            if(!noReading && !reviewWord.getJpWriting().equals(reviewWord.getJpReading())){
                qc.setJpWriting(reviewWord.getJpWriting() + " (" + reviewWord.getJpReading() + ")");
                reviewer.add(reviewWord.getJpWriting() + " (" + reviewWord.getJpReading() + ") - " + reviewWord.getMeaning());
            } else {
                qc.setJpWriting(reviewWord.getJpWriting());
                reviewer.add(reviewWord.getJpWriting() + " - " + reviewWord.getMeaning());
            }
            qc.setNumber(i + 1);
//            qc.setNumber(i + 1);
            qc.setId(Math.toIntExact(reviewWord.getInsertOrder()));

            // Add choices
            List<String> choices = new ArrayList<>();
            if(choiceReading){
                addChoices(choices, reviewWordOptional, currentId, random, Vocabulary::getJpReading);
            } else {
                addChoices(choices, reviewWordOptional, currentId, random, Vocabulary::getMeaning);
            }

            Collections.shuffle(choices, random);

            qc.setA(choices.get(0));
            qc.setB(choices.get(1));
            qc.setC(choices.get(2));
            qc.setD(choices.get(3));

            qcList.add(qc);
        }

        Collections.shuffle(reviewer, random);
        System.out.println(out);
        for (int j = 1; j <= reviewer.size(); j++) {
            System.out.println(j + ". " + reviewer.get(j - 1));
        }
        voice_num_list.forEach(System.out::println);
        return qcList;
    }

//    public List<QuestionAndChoices> customQuestions(){
//        List<String> reviewer = new ArrayList<>();
////        List<Integer> quizItems = new ArrayList<>(List.of(
////                3, 17, 25, 30, 35, 39, 46, 47, 62, 76,
////                81, 82, 84, 98, 116, 122, 124, 135, 137, 138,
////                140, 161, 164, 174, 175, 202, 205, 219, 228, 231,
////                234, 248, 249, 260, 261, 267, 276, 278, 283, 294,
////                297, 298, 309, 310, 319, 323, 324, 326, 328, 334,
////                337, 341, 344, 346, 350, 351, 353, 358, 359, 391,
////                395, 399, 400, 401, 402, 407, 408, 409, 411, 419,
////                420, 431, 435, 436, 446, 449, 452, 459, 460, 463,
////                469, 470, 471, 479, 496, 498, 504, 506, 523, 540,
////                573, 593, 601, 602, 617, 618, 1, 2, 3, 4
////        ));
//        int numberOfItems = 100;
//        List<Integer> quizItems = new ArrayList<>();
//        Random random = new Random(27032025);
//        //Collections.shuffle(quizItems, random);
//        while(quizItems.size()<numberOfItems){
//            int id = random.nextInt(vocabularyRepository.findMaxInsertOrder()) + 1;
//            if(!quizItems.contains(id)){
//                quizItems.add(id);
//            }
//        }
//        List<QuestionAndChoices> qcList = new ArrayList<>();
//        for(int i = 0; i < quizItems.size(); i++){
//            //int id = random.nextInt(reviewWordRepository.findMaxInsertOrder()) + 1;
//            QuestionAndChoices qc = new QuestionAndChoices();
//            Optional<Vocabulary> reviewWordOptional = vocabularyRepository.findById(quizItems.get(i));
//            String questionItem = reviewWordOptional
//                    .map(Vocabulary::getJpWriting) // Get jpWriting if Vocabulary exists
//                    .orElse(null);
//            String questionAnswer = reviewWordOptional
//                    .map(Vocabulary::getMeaning)
//                    .orElse(null);
//            reviewer.add(questionItem +  " - " + questionAnswer);
//
//            qc.setJpWriting(reviewWordOptional
//                    .map(Vocabulary::getJpWriting) //getKanjiWriting) // Get jpWriting if Vocabulary exists
//                    .orElse(null));
//            qc.setNumber(i+1);
//            qc.setId(Math.toIntExact(reviewWordOptional
//                    .map(Vocabulary::getInsertOrder) // Get id if Vocabulary exists
//                    .orElse(null)));
//
//
//            List<String> choices = new ArrayList<>();
//
////            choices.add(reviewWordOptional
////                    .map(Vocabulary::getMeaning)
////                    .orElse(null));
////            for(int j = 0; j < 3; j++){
////                int wrongChoiceId = idRandDifferentiator(quizItems.get(i), random, choices);
////                Optional<Vocabulary> optWord = vocabularyRepository.findById(wrongChoiceId);
////                choices.add(optWord
////                        .map(Vocabulary::getMeaning) // Suggestion USE AI TO ASSESS IF SAME MEANING
////                        .orElse(null));
////            }
//            //int g = 0;
////            if(random.nextBoolean()){
//                addChoices(choices, reviewWordOptional, quizItems.get(i), random, Vocabulary::getMeaning);
////            } else {
////                addChoices(choices, reviewWordOptional, quizItems.get(i), random, Vocabulary::toRomaji);
////            }
//
//
//
//            //choiceShuffleSeed = (7*choiceShuffleSeed+500)%seed;
//            Collections.shuffle(choices, random);
//
//
//            qc.setA(choices.get(0));// getVocabularyById(id);
//            qc.setB(choices.get(1));
//            qc.setC(choices.get(2));
//            qc.setD(choices.get(3));
//            qcList.add(qc);
//        }
//
//        Collections.shuffle(reviewer, random);
//        for(int j = 1; j <= reviewer.size(); j++){
//            System.out.println(j + ". " + reviewer.get(j-1));
//        }
//        return qcList;/////////////////////////////RETURN!
//    }

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
            Optional<Vocabulary> reviewWordOptional = vocabularyRepository.findById(questionId);
            String correctAnswer = reviewWordOptional
                    .map(Vocabulary::getMeaning)
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

    public Optional<Vocabulary> getVocabularyById(int id) {
        return vocabularyRepository.findById(id);
    }

    public Vocabulary createVocabulary(Vocabulary vocabulary) {
        return vocabularyRepository.save(vocabulary);
    }

    public Vocabulary updateVocabulary(int id, Vocabulary vocabularyDetails) {
        Vocabulary vocabulary = vocabularyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vocabulary not found"));

        vocabulary.setJpWriting(vocabularyDetails.getJpWriting());
        vocabulary.setMeaning(vocabularyDetails.getMeaning());
        vocabulary.setCategory(vocabularyDetails.getCategory());

        return vocabularyRepository.save(vocabulary);
    }

    public void deleteVocabulary(int id) {
        Vocabulary vocabulary = vocabularyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vocabulary not found"));
        vocabularyRepository.delete(vocabulary);
    }



    public QuizResultResponse processQuizSubmission(QuizSubmission submission, boolean isMultipleChoice) throws Exception {
        String name = submission.getName();
        List<Answer> answers = submission.getAnswers();
        int correctAnswers = 0;
        List<MistakeDetail> detailedMistakes = new ArrayList<>();

        int k = 1;
        for (Answer answer : answers) {
            int questionId = answer.getId();
            answer.setNumber(k);
            String userAnswer = answer.getAnswer();
            Optional<Vocabulary> vocabularyOptional = getVocabularyById(questionId);

            String correctAnswer;
            String correctAnswerQ = vocabularyOptional  //the Question
                    .map(Vocabulary::getJpWriting)
                    .orElse(null);
            if(choiceReading && isMultipleChoice){      //if multiple choice question and the contents are reading
                correctAnswer = vocabularyOptional
                        .map(Vocabulary::getJpReading)
                        .orElse(null);
            } else {
                correctAnswer = vocabularyOptional
                        .map(Vocabulary::getMeaning)
                        .orElse(null);
            }

            boolean isCorrect;
            if (isMultipleChoice) {
                isCorrect = (correctAnswer != null && correctAnswer.equals(userAnswer));
            } else {
                List<List<Float>> meaningEmbeddings = vocabularyOptional
                        .map(Vocabulary::getMeaningEmbedding)
                        .orElse(new ArrayList<>());
                List<Float> userEmbedding = embeddingService.getEmbedding(userAnswer);

                float maxCS = 0f;
                for (List<Float> meaningEmbedding : meaningEmbeddings) {
                    float cs = embeddingService.cosineSimilarity(userEmbedding, meaningEmbedding);
                    if (cs > maxCS) maxCS = cs;
                }

                double threshold;
                int totStrLen = userAnswer.length() + correctAnswer.length();
                if (1.5 * userAnswer.length() < correctAnswer.length()) {
                    threshold = 0.53 + (0.98 - 0.53) * Math.exp((double) - userAnswer.length() * 2 / 7);
                } else {
                    threshold = 0.53 + (0.98 - 0.53) * Math.exp((double) - totStrLen / 7);
                }

                isCorrect = maxCS >= threshold;
            }

            if (isCorrect) {
                correctAnswers++;
            } else {
                detailedMistakes.add(new MistakeDetail(k, correctAnswerQ + " (" + vocabularyOptional
                        .map(Vocabulary::getJpReading)
                        .orElse(null) + ") - " +  userAnswer + "❌", correctAnswer + "✅"));
            }

            k++;
        }

        int score = correctAnswers * 100 / answers.size();

        // ✅ Save to DB with numbers only
        List<String> mistakeNumbers = detailedMistakes.stream()
                .map(m -> String.valueOf(m.getNumber()))
                .collect(Collectors.toList());

        QuizResult quizResult = new QuizResult(name, score, mistakeNumbers, LocalDateTime.now());
        quizResultRepository.save(quizResult);

        // ✅ Return full details to frontend
        return new QuizResultResponse(name, score, detailedMistakes, LocalDateTime.now());
    }

}