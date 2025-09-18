Prerequisites
- React.js
- Java
- Ollama - nomic-embed-text model
- VoiceVox
- Python (optional for altering embeddings)

HOW TO RUN:

backend:
1. cd backend
2. php start_server.php

frontend:
1. npm run dev

ai:
1. ollama serve

tts:
1. make sure voicevox is installed correctly



Useful tools
how to add vectors (embeddings):
1. cd pythonScripts
2. python embedSetup.py
or (deprecated)
1. cd backend
2. php artisan embeddings:generate

how to test cosine similarity of two strings:
1. cd pythonScripts
2. python embedTest.py

how to add parts of speech:
1. cd backend
2. php artisan words:fill-pos

how to update jlpt core vocab (in backend/database)
1. cd backend
2. php sync_jlpt_decks.php

adding words manually:
-- INSERT INTO words 
-- (japanese, reading, english, jlpt_level, example_sentence, part_of_speech, created_at, updated_at, embedding)
-- VALUES
-- ('<jp>', '<reading>', '<meaning>', '<jlpt>', NULL, 'verb', NOW(), NOW(), NULL);



