from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from models import ChatRequest, ChatResponse, DocumentIngestRequest
from agent import process_query
from rag_utils import add_document
from collections import deque

app = FastAPI(title="Interactive Safety AI Agent")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# ── In-memory conversation history (per session_id, last 20 turns) ───────────
_sessions: dict[str, deque] = {}

def get_history(session_id: str) -> list:
    return list(_sessions.get(session_id, []))

def save_turn(session_id: str, user_msg: str, ai_reply: str):
    if session_id not in _sessions:
        _sessions[session_id] = deque(maxlen=20)
    _sessions[session_id].append({"role": "user",      "content": user_msg})
    _sessions[session_id].append({"role": "assistant", "content": ai_reply})

@app.post("/chat", response_model=ChatResponse)
async def chat_endpoint(request: ChatRequest):
    try:
        history = get_history(request.session_id or "default")
        result  = process_query(request.query, history=history)
        reply   = result.get("reply", "")
        # persist this turn
        save_turn(request.session_id or "default", request.query, reply)
        return ChatResponse(reply=reply, action=result.get("action"))
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/ingest")
async def ingest_document(request: DocumentIngestRequest):
    try:
        add_document(request.text, request.metadata)
        return {"status": "success", "message": "Document added to knowledge base."}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.delete("/session/{session_id}")
async def clear_session(session_id: str):
    _sessions.pop(session_id, None)
    return {"status": "cleared"}

@app.get("/health")
async def health_check():
    return {"status": "healthy", "sessions_active": len(_sessions)}
