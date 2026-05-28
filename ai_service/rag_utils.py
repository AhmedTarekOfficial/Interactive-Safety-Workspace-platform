import os
from langchain_chroma import Chroma
from langchain_huggingface import HuggingFaceEmbeddings
from langchain_core.documents import Document

CHROMA_DB_DIR = os.path.join(os.path.dirname(__file__), "chroma_db")

# We use an open-source embedding model from Hugging Face (downloads locally)
embeddings = HuggingFaceEmbeddings(model_name="sentence-transformers/all-MiniLM-L6-v2")

def get_vector_store():
    # Load or initialize the Chroma vector store
    return Chroma(persist_directory=CHROMA_DB_DIR, embedding_function=embeddings)

def add_document(text: str, metadata: dict = None):
    vector_store = get_vector_store()
    doc = Document(page_content=text, metadata=metadata or {})
    vector_store.add_documents([doc])

def retrieve_documents(query: str, top_k: int = 3):
    vector_store = get_vector_store()
    return vector_store.similarity_search(query, k=top_k)
