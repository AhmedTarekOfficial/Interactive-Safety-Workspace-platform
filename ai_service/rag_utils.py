import os
from langchain_chroma import Chroma
from langchain_huggingface import HuggingFaceEmbeddings
from langchain_core.documents import Document

CHROMA_DB_DIR = os.path.join(os.path.dirname(__file__), "chroma_db")

# ── Multilingual embedding model (supports Arabic + English) ─────────────────
# Replaced: "sentence-transformers/all-MiniLM-L6-v2"  (English-only)
# With:     "sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2"
#           — supports 50+ languages including Arabic
embeddings = HuggingFaceEmbeddings(
    model_name="sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2",
    model_kwargs={"device": "cpu"},
    encode_kwargs={"normalize_embeddings": True},
)

def get_vector_store():
    return Chroma(persist_directory=CHROMA_DB_DIR, embedding_function=embeddings)

def add_document(text: str, metadata: dict = None):
    vector_store = get_vector_store()
    doc = Document(page_content=text, metadata=metadata or {})
    vector_store.add_documents([doc])

def retrieve_documents(query: str, top_k: int = 4):
    """
    Returns top_k most relevant documents.
    Falls back to empty list if DB is empty or unavailable.
    """
    try:
        vector_store = get_vector_store()
        results = vector_store.similarity_search_with_relevance_scores(query, k=top_k)
        # filter out low-relevance results (score < 0.3)
        return [doc for doc, score in results if score >= 0.3]
    except Exception as e:
        print(f"[RAG Warning] {e}")
        return []
