package com.example.demo.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
public class QuizSubmission {
    private String name;
    private List<Answer> answers;
}