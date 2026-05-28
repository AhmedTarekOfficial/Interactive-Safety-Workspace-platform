from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from models import ChatRequest, ChatResponse, DocumentIngestRequest
from agent import process_query
from rag_utils import add_document

app = FastAPI(title="Interactive Safety AI Agent")

# Allow CORS for the frontend
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # Adjust in production
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

@app.post("/chat", response_model=ChatResponse)
async def chat_endpoint(request: ChatRequest):
    try:
        # Pass the query to our LangChain/LangGraph agent
        result = process_query(request.query)
        return ChatResponse(reply=result.get("reply", ""), action=result.get("action"))
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/ingest")
async def ingest_document(request: DocumentIngestRequest):
    try:
        add_document(request.text, request.metadata)
        return {"status": "success", "message": "Document added to knowledge base."}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/health")
async def health_check():
    return {"status": "healthy"}
