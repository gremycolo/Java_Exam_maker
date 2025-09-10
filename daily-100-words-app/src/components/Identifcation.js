// src/components/Identification.js
import React, { useState, useEffect } from "react";

const Identification = () => {
  const [questions, setQuestions] = useState([]);
  const [answers, setAnswers] = useState({});
  const [loading, setLoading] = useState(true);
  const [submitted, setSubmitted] = useState(false);
  const [quizResult, setQuizResult] = useState(null);

  const ngrokLink = process.env.REACT_APP_API_URL;

  useEffect(() => {
    const fetchQuestions = async () => {
      try {
        const res = await fetch(`${ngrokLink}/api/vocabulary/100Words`);
        const data = await res.json();
        // initialize answers map empty for each id to keep selection logic consistent
        const initialAnswers = {};
        data.forEach((q) => {
          initialAnswers[q.id] = "";
        });
        setAnswers(initialAnswers);
        setQuestions(data);
        setLoading(false);
      } catch (err) {
        console.error("Error fetching identification questions:", err);
        setLoading(false);
      }
    };

    fetchQuestions();
  }, [ngrokLink]);

  const handleAnswerChange = (id, value) => {
    setAnswers((prev) => ({ ...prev, [id]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (submitted) return;

    const submission = {
      name: "", // optional: include name if you want to add it
      answers: questions.map((q) => ({
        id: q.id,
        answer: (answers[q.id] || "").trim(),
      })),
    };

    try {
      const res = await fetch(
        `${ngrokLink}/api/vocabulary/api/submitIdentificationQuiz`,
        {
          method: "PUT",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(submission),
        }
      );

      // backend in your project used plain text responses earlier for multiple choice
      // here we try to parse JSON; if backend returns text then adapt accordingly
      const contentType = res.headers.get("content-type") || "";
      let result;
      if (contentType.includes("application/json")) {
        result = await res.json();
      } else {
        // try text -> fallback parsing the message
        const text = await res.text();
        // keep any human message for debugging
        console.log("SubmitIdentificationQuiz response text:", text);
        // fallback: try to extract score/mistakes like your original component did
        const scoreMatch = text.match(/Score: (\d+)%/);
        const mistakesMatch = text.match(/Mistakes: ([\d, ]+)/);
        const score = scoreMatch ? parseInt(scoreMatch[1], 10) : 0;
        const mistakes = mistakesMatch ? mistakesMatch[1].split(", ").map(Number) : [];
        result = { score, mistakes };
      }

      setQuizResult(result);
      setSubmitted(true);
    } catch (err) {
      console.error("Error submitting identification quiz:", err);
    }
  };

  if (loading) return <div style={styles.loading}>Loading questions...</div>;

  // helpers for styling like in MultipleChoice
  const isMistakeForNumber = (questionNumber) => {
    if (!quizResult) return false;
    // Support both formats: mistakes array of numbers OR array of objects (ids)
    if (Array.isArray(quizResult.mistakes)) {
      // numeric list
      return quizResult.mistakes.includes(questionNumber);
    }
    return false;
  };

  return (
    <div style={styles.container}>
      <h1 style={styles.header}>Japanese Vocabulary Quiz</h1>

      <form onSubmit={handleSubmit}>
        // inside the form, above the questions
        <div style={styles.nameInput}>
        <label style={styles.label}>
            Your Name:
            <input
            type="text"
            value={answers.name || ""}
            onChange={(e) => setAnswers((prev) => ({ ...prev, name: e.target.value }))}
            placeholder="Enter your name"
            style={styles.input}
            onFocus={(e) => (e.target.style.borderColor = styles.inputFocus.borderColor)}
            onBlur={(e) => (e.target.style.borderColor = styles.input.borderColor)}
            required
            disabled={submitted}
            />
        </label>
        </div>
        {questions.map((q, index) => {
          const questionNumber = index + 1;
          const isMistake = isMistakeForNumber(questionNumber);
          const isSelected = (answers[q.id] || "").trim() !== "";
          const isCorrect = submitted && !isMistake && isSelected;

          const questionStyle = submitted
            ? isMistake
              ? styles.questionIncorrect
              : isCorrect
              ? styles.questionCorrect
              : styles.question
            : isSelected
            ? styles.questionSelected
            : styles.question;

          return (
            <div key={q.id} style={questionStyle}>
              <h3 style={styles.questionText}>
                Question {questionNumber}: {q.jpWriting}
              </h3>

              <div style={styles.choice}>
                <input
                  type="text"
                  style={styles.input}
                  value={answers[q.id] || ""}
                  onChange={(e) => handleAnswerChange(q.id, e.target.value)}
                  placeholder="Type your answer..."
                  disabled={submitted}
                />
              </div>
            </div>
          );
        })}

        <button
            type="submit"
            disabled={
                submitted ||
                !answers.name || // disable if name empty
                questions.some((q) => !(answers[q.id] || "").trim()) // disable if any question empty
            }
            style={
                submitted ||
                !answers.name ||
                questions.some((q) => !(answers[q.id] || "").trim())
                ? styles.buttonDisabled
                : styles.button
            }
            onMouseOver={(e) =>
                !submitted &&
                answers.name &&
                questions.every((q) => (answers[q.id] || "").trim()) &&
                (e.currentTarget.style.backgroundColor = styles.buttonHover.backgroundColor)
            }
            onMouseOut={(e) =>
                !submitted &&
                answers.name &&
                questions.every((q) => (answers[q.id] || "").trim()) &&
                (e.currentTarget.style.backgroundColor = styles.button.backgroundColor)
            }
            >
            Submit Answers
            </button>

        {quizResult && (
          <div style={styles.resultContainer}>
            <div style={styles.scoreText}>
              Your Score: {quizResult.score ?? "0"}%
            </div>
            {quizResult.mistakes && quizResult.mistakes.length > 0 && (
              <>
                <h4>Mistakes (Question Numbers):</h4>
                <div style={styles.mistakeItem}>
                  <span style={styles.mistakeText}>
                    {Array.isArray(quizResult.mistakes)
                      ? quizResult.mistakes.join(", ")
                      : JSON.stringify(quizResult.mistakes)}
                  </span>
                </div>
              </>
            )}
          </div>
        )}
      </form>
    </div>
  );
};

// — exact same styles object used in your MultipleChoice / App.js
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
    width: "100%", // adapted to fill the container for identification
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

export default Identification;
