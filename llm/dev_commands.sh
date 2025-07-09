# test docker container
docker build --pull --build-arg MODEL=$MODEL . -t gemma3
docker run -d -p 8080:8080 --name gemma-proxy -e MODEL=$MODEL gemma3

curl -X POST https://fuzzy-system-rwgpxxv44pj25xrj-8080.app.github.dev/v1beta/models/gemma-3-4b-it:generateContent \
  -H "Content-Type: application/json" \
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

# Go run tests
go mod tidy
go mod download
go test -v