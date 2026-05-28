@echo off
echo Starting Interactive Safety AI Agent (Python Microservice)...
start cmd /k "cd ai_service && pip install -r requirements.txt && python -m uvicorn main:app --port 8000"

echo Starting Spring Boot Backend...
start cmd /k "mvnw.cmd spring-boot:run"

echo Both servers are starting in separate windows!
