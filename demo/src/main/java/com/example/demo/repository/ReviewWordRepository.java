// src/main/java/com/example/demo/repository/ReviewWordRepository.java
package com.example.demo.repository;

import com.example.demo.entity.ReviewWord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ReviewWordRepository extends JpaRepository<ReviewWord, Long> {
    @Query("SELECT MAX(v.id) FROM ReviewWord v")
    Integer findMaxInsertOrder();
}