// src/components/MultipleChoice.js
import React, { useState, useEffect } from "react";
import QuizResultDisplay from "./QuizResultDisplay";

const MultipleChoice = () => {
  const [questions, setQuestions] = useState([]);
  const [answers, setAnswers] = useState({});
  const [name, setName] = useState("");
  const [isLoading, setIsLoading] = useState(true);
  const [quizResult, setQuizResult] = useState(null);
  const [isSubmitted, setIsSubmitted] = useState(false);
  const ngrokLink = process.env.REACT_APP_API_URL;

  useEffect(() => {
    const initialAnswers = {};
    setAnswers(initialAnswers);

    fetch(ngrokLink + "/api/vocabulary/100Words")
      .then((response) => response.json())
      .then((data) => {
        setAnswers({}); // will fill by user interaction
        setQuestions(data);
        setIsLoading(false);
      })
      .catch((error) => {
        console.error("Error fetching questions:", error);
        setIsLoading(false);
      });
  }, [ngrokLink]);

  const handleChoiceChange = (questionIndex, choiceKey) => {
    if (!isSubmitted) {
      setAnswers((prevAnswers) => ({
        ...prevAnswers,
        [questionIndex + 1]: choiceKey,
      }));
    }
  };

  const handleNameChange = (e) => {
    if (!isSubmitted) {
      setName(e.target.value);
    }
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    if (isSubmitted) return;

    const submittedData = {
      name: name,
      answers: questions.map((question, index) => ({
        id: question.id,
        jpWriting: question.jpWriting,
        // chosen option key (a/b/c/d) => get that field from question; fallback to question.a
        answer: question[answers[index + 1]] || question.a,
      })),
    };

    // Expect JSON response (QuizResultResponse)
    fetch(ngrokLink + "/api/vocabulary/api/submitQuiz", {
      method: "PUT",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(submittedData),
    })
      .then((response) => response.json()) // << always parse JSON now
      .then((result) => {
        console.log("Success:", result);
        setQuizResult(result);
        setIsSubmitted(true);
        alert("Answers submitted successfully! Check your score below.");
      })
      .catch((error) => {
        console.error("Error:", error);
        alert("Error submitting answers. Check the console.");
      });
  };

  const styles = {
    container: {
      maxWidth: "800px",
      margin: "0 auto",
      padding: "20px",
      backgroundColor: "#f9f9f9",
      borderRadius: "10px",
      boxShadow: "0 0 10px rgba(0, 0, 0, 0.1)",
      fontFamily: "Arial, sans-serif",
    },
    header: {
      textAlign: "center",
      color: "#2c3e50",
      marginBottom: "20px",
      fontSize: "2.5em",
      fontWeight: "bold",
    },
    nameInput: {
      display: "flex",
      alignItems: "center",
      marginBottom: "20px",
    },
    label: {
      fontSize: "1.2em",
      color: "#34495e",
      marginRight: "10px",
    },
    input: {
      padding: "10px",
      fontSize: "1em",
      border: "2px solid #3498db",
      borderRadius: "5px",
      width: "200px",
      outline: "none",
      transition: "border-color 0.3s",
    },
    inputFocus: {
      borderColor: "#2980b9",
    },
    question: {
      backgroundColor: "#ffffff",
      padding: "15px",
      borderRadius: "5px",
      marginBottom: "15px",
      borderLeft: "5px solid #ffffff",
    },
    questionSelected: {
      backgroundColor: "#ffffff",
      padding: "15px",
      borderRadius: "5px",
      marginBottom: "15px",
      borderLeft: "5px solid #f1c40f",
    },
    questionCorrect: {
      backgroundColor: "#ffffff",
      padding: "15px",
      borderRadius: "5px",
      marginBottom: "15px",
      borderLeft: "5px solid #2ecc71",
    },
    questionIncorrect: {
      backgroundColor: "#ffffff",
      padding: "15px",
      borderRadius: "5px",
      marginBottom: "15px",
      borderLeft: "5px solid #e74c3c",
    },
    questionText: {
      color: "#2c3e50",
      fontSize: "1.3em",
      marginBottom: "10px",
    },
    choice: {
      margin: "5px 0",
    },
    radio: {
      marginRight: "10px",
    },
    optionText: {
      color: "#7f8c8d",
      fontSize: "1.1em",
    },
    button: {
      backgroundColor: "#e74c3c",
      color: "#ffffff",
      padding: "10px 20px",
      border: "none",
      borderRadius: "5px",
      fontSize: "1.1em",
      cursor: "pointer",
      transition: "background-color 0.3s",
    },
    buttonHover: {
      backgroundColor: "#c0392b",
    },
    buttonDisabled: {
      backgroundColor: "#bdc3c7",
      cursor: "not-allowed",
    },
    loading: {
      textAlign: "center",
      color: "#7f8c8d",
      fontSize: "1.2em",
    },
    resultContainer: {
      marginTop: "20px",
      padding: "15px",
      backgroundColor: "#fff",
      borderRadius: "5px",
      borderLeft: "5px solid #3498db",
    },
    scoreText: {
      fontSize: "1.5em",
      color: "#2ecc71",
      marginBottom: "10px",
    },
    mistakeItem: {
      margin: "10px 0",
      padding: "10px",
      backgroundColor: "#f2dede",
      borderRadius: "5px",
      border: "1px solid #ebccd1",
    },
    mistakeText: {
      color: "#a94442",
      fontSize: "1.1em",
    },
  };

  if (isLoading) return <div style={styles.loading}>Loading questions...</div>;

  return (
    <div style={styles.container}>
      <h1 style={styles.header}>Japanese Vocabulary Quiz</h1>
      <form onSubmit={handleSubmit}>
        <div style={styles.nameInput}>
          <label style={styles.label}>
            Your Name:
            <input
              type="text"
              value={name}
              onChange={handleNameChange}
              placeholder="Enter your name"
              style={styles.input}
              onFocus={(e) => (e.target.style.borderColor = styles.inputFocus.borderColor)}
              onBlur={(e) => (e.target.style.borderColor = styles.input.borderColor)}
              required
              disabled={isSubmitted}
            />
          </label>
        </div>

        {questions.map((question, index) => {
          const questionNumber = index + 1;
          // now mistakes are objects, so use .some to check
          const isMistake = quizResult?.mistakes?.some((m) => m.number === questionNumber);
          const isCorrect = quizResult && !isMistake && answers[questionNumber];
          const isSelected = !!answers[questionNumber];

          return (
            <div
              key={index}
              style={
                isSubmitted
                  ? isMistake
                    ? styles.questionIncorrect
                    : isCorrect
                    ? styles.questionCorrect
                    : styles.question
                  : isSelected
                  ? styles.questionSelected
                  : styles.question
              }
            >
              <h3 style={styles.questionText}>
                Question {questionNumber}: {question.jpWriting}
              </h3>

              <div style={styles.choice}>
                <label>
                  <input
                    type="radio"
                    name={`question-${index}`}
                    value="a"
                    checked={answers[questionNumber] === "a"}
                    onChange={() => handleChoiceChange(index, "a")}
                    style={styles.radio}
                    disabled={isSubmitted}
                  />
                  <span style={styles.optionText}>{question.a}</span>
                </label>
              </div>
              <div style={styles.choice}>
                <label>
                  <input
                    type="radio"
                    name={`question-${index}`}
                    value="b"
                    checked={answers[questionNumber] === "b"}
                    onChange={() => handleChoiceChange(index, "b")}
                    style={styles.radio}
                    disabled={isSubmitted}
                  />
                  <span style={styles.optionText}>{question.b}</span>
                </label>
              </div>
              <div style={styles.choice}>
                <label>
                  <input
                    type="radio"
                    name={`question-${index}`}
                    value="c"
                    checked={answers[questionNumber] === "c"}
                    onChange={() => handleChoiceChange(index, "c")}
                    style={styles.radio}
                    disabled={isSubmitted}
                  />
                  <span style={styles.optionText}>{question.c}</span>
                </label>
              </div>
              <div style={styles.choice}>
                <label>
                  <input
                    type="radio"
                    name={`question-${index}`}
                    value="d"
                    checked={answers[questionNumber] === "d"}
                    onChange={() => handleChoiceChange(index, "d")}
                    style={styles.radio}
                    disabled={isSubmitted}
                  />
                  <span style={styles.optionText}>{question.d}</span>
                </label>
              </div>
            </div>
          );
        })}

        <button
          type="submit"
          disabled={isSubmitted || Object.keys(answers).length < questions.length || !name}
          style={
            isSubmitted || Object.keys(answers).length < questions.length || !name
              ? styles.buttonDisabled
              : styles.button
          }
          onMouseOver={(e) =>
            !isSubmitted &&
            Object.keys(answers).length >= questions.length &&
            !!name &&
            (e.target.style.backgroundColor = styles.buttonHover.backgroundColor)
          }
          onMouseOut={(e) =>
            !isSubmitted &&
            Object.keys(answers).length >= questions.length &&
            !!name &&
            (e.target.style.backgroundColor = styles.button.backgroundColor)
          }
        >
          Submit Answers
        </button>

        {/* Use shared QuizResultDisplay */}
        <QuizResultDisplay quizResult={quizResult} styles={styles} />
      </form>
    </div>
  );
};

export default MultipleChoice;
