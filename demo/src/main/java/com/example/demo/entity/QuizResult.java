package com.example.demo.entity;

import com.example.demo.converter.StringListConverter;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "quiz_results")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "score")
    private int score;

    @Column(name = "mistakes", columnDefinition = "NVARCHAR(MAX)")
    @Convert(converter = StringListConverter.class)
    private List<String> mistakes;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    // Custom constructor without id
    public QuizResult(String name, int score, List<String> mistakes, LocalDateTime submittedAt) {
        this.name = name;
        this.score = score;
        this.mistakes = mistakes;
        this.submittedAt = submittedAt;
    }
}