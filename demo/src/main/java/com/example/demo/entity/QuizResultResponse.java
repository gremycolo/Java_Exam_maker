package com.example.demo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuizResultResponse {
    private String name;
    private int score;
    private List<MistakeDetail> mistakes; // full detail for frontend
    private LocalDateTime submittedAt;
}
