docker build --pull --build-arg MODEL=gemma3:4b . -t gemma3:4b
docker run -d --name gemma-proxy -p 8080:8080 -e API_KEY="dummy" gemma3-4b

curl -X POST https://obscure-space-fishstick-6wg4pp79gj5hxvjw-8080.app.github.dev/v1beta/models/gemma-3-4b-it:generateContent \
  -H "Content-Type: application/json" \
  -H "x-goog-api-key: dummy" \
  -d '{
    "contents": [
      {
        "parts": [
          {"text": "Yes or no: Is Python case-sensitive?"}
        ]
      }
    ],
    "generationConfig": {
      "maxOutputTokens": 100,
      "temperature": 0.7
    }
  }'