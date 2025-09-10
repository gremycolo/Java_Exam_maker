package com.example.demo.repository;

import com.example.demo.entity.Vocabulary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface VocabularyRepository extends JpaRepository<Vocabulary, Integer> {
    @Query("SELECT MAX(v.insertOrder) FROM Vocabulary v")
    Integer findMaxInsertOrder();
    // fetch all items by level
    List<Vocabulary> findByJlptLevel(String jlptLevel);
    List<Vocabulary> findByJlptLevelAndIsEntirelyKatakana(String jlptLevel, boolean isKatakana);
    List<Vocabulary> findByIsEntirelyKatakana(boolean isKatakana);
}