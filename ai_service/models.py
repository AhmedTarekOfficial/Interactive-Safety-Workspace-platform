from pydantic import BaseModel
from typing import Optional, Dict, Any

class ChatRequest(BaseModel):
    query: str
    session_id: Optional[str] = "default"
    
class ChatResponse(BaseModel):
    reply: str
    action: Optional[Dict[str, Any]] = None

class DocumentIngestRequest(BaseModel):
    text: str
    metadata: Optional[Dict[str, Any]] = None
