// src/components/QuizResultDisplay.js
import React from "react";

const QuizResultDisplay = ({ quizResult, styles }) => {
  if (!quizResult) return null;

  return (
    <div style={styles.resultContainer}>
      <div style={styles.scoreText}>
        Your Score: {quizResult.score ?? 0}%
      </div>

      {quizResult.mistakes && quizResult.mistakes.length > 0 ? (
        <>
          <h4>Mistakes:</h4>
          {quizResult.mistakes.map((m, idx) => (
            <div key={idx} style={styles.mistakeItem}>
              <span style={styles.mistakeText}>
                Q{m.number}: {m.jpWriting} → {m.meaning}
              </span>
            </div>
          ))}
        </>
      ) : (
        <div style={{ marginTop: 10 }}>
          <strong>Nice!</strong> No mistakes found.
        </div>
      )}
    </div>
  );
};

export default QuizResultDisplay;
