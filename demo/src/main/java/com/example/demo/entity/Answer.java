package com.example.demo.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Answer {
    private int number;
    private int id;
    private String jpWriting;
    private String answer; // Holds the full string (e.g., "cat")
}
