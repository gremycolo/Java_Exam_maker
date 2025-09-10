package com.example.demo.entity;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "review_words")
@Data
public class ReviewWord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "jpWriting", nullable = false, columnDefinition = "NVARCHAR(255)")
    private String jpWriting;

    @Column(name = "meaning", nullable = false, columnDefinition = "NVARCHAR(255)")
    private String meaning;
}