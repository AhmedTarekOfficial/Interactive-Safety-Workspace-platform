import os
import json
import re
from huggingface_hub import InferenceClient
from rag_utils import retrieve_documents
from dotenv import load_dotenv

load_dotenv(override=False)

# ─── Models ───────────────────────────────────────────────────────────────────
# Arabic / mixed → Qwen2.5-72B  (best Arabic + Egyptian dialect)
# English only   → Mistral-7B-Instruct (sharp, safety-domain English)
MODEL_ARABIC  = "Qwen/Qwen2.5-7B-Instruct"
MODEL_ENGLISH = "Qwen/Qwen2.5-7B-Instruct"

def get_client(model: str) -> InferenceClient:
    return InferenceClient(
        model=model,
        token=os.environ.get("HF_TOKEN"),
    )

# ─── Language Detection ───────────────────────────────────────────────────────
def detect_language(text: str) -> str:
    """
    Returns 'ar' if the text contains Arabic characters, 'en' otherwise.
    Simple and dependency-free — no need for langdetect library.
    """
    arabic_chars = re.findall(r'[\u0600-\u06FF]', text)
    ratio = len(arabic_chars) / max(len(text), 1)
    return "ar" if ratio > 0.15 else "en"

# ─── System Prompts ───────────────────────────────────────────────────────────
SYSTEM_PROMPT_AR = """أنت مساعد متخصص في معايير ولوائح السلامة المهنية (Occupational Health & Safety).

تخصصك يشمل:
- معايير OSHA (Occupational Safety and Health Administration)
- معيار ISO 45001 لأنظمة إدارة السلامة والصحة المهنية
- متطلبات PPE (Personal Protective Equipment) لكل بيئة عمل
- إجراءات العمل الآمن (Safe Work Procedures)
- تقييم المخاطر (Risk Assessment) وإدارتها
- الإسعافات الأولية ومتطلبات الطوارئ
- معايير السلامة من الحريق (Fire Safety Standards)
- سلامة الكهرباء والمعدات الثقيلة

قواعد الرد:
1. رد بنفس لغة المستخدم — عربي أو عامية مصرية حسب ما يكتب.
2. المصطلحات التقنية الدولية (PPE, OSHA, ISO, LTI, MSDS...) اكتبها بالإنجليزي دايماً حتى في الرد العربي.
3. نظّم ردك دايماً بالشكل ده:
   - سطر مقدمة قصيرة
   - سطر فاضي
   - bullet points أو تعداد لو فيه أكتر من نقطة
   - سطر فاضي بين كل فكرة وتانية
   - خلاصة أو تحذير مهم في الآخر لو لازم
4. متخترعش معلومات — لو السياق مش كافي قول "مفيش معلومات كافية عن ده في قاعدة البيانات دلوقتي."
5. لو المستخدم بيسلم أو بيتكلم اجتماعي، رد بشكل طبيعي ومحترم.
6. لو طلب ينتقل لصفحة، حط الـ action المناسب.

الـ output لازم يكون JSON فقط بالشكل ده:
{
  "reply": "ردك هنا...\n\n• نقطة أولى\n• نقطة تانية\n\nخلاصة...",
  "action": {"type": "navigate", "url": "/الصفحة"}
}
لو مفيش navigation:
{
  "reply": "ردك هنا...",
  "action": null
}

الصفحات المتاحة:
- /index | /training | /training/courses | /first_aid | /fire_station | /safety_alerts | /admin | /Login

JSON فقط — بدون أي كلام قبلها أو بعدها."""


SYSTEM_PROMPT_EN = """You are a specialized assistant in Occupational Health & Safety (OHS) regulations and standards.

Your expertise covers:
- OSHA (Occupational Safety and Health Administration) standards & 29 CFR regulations
- ISO 45001 Occupational Health & Safety Management Systems
- PPE (Personal Protective Equipment) selection and requirements
- Safe Work Procedures (SWP) and Job Hazard Analysis (JHA)
- Risk Assessment methodologies (5x5 matrix, HIRA, HAZOP)
- First Aid requirements and emergency response procedures
- Fire Safety Standards (NFPA, local codes)
- Electrical safety, lockout/tagout (LOTO), and heavy equipment safety
- Incident investigation and LTI (Lost Time Injury) reporting

Response formatting rules:
1. Always respond in clear, professional English.
2. Structure every response as:
   - One short introductory sentence
   - (blank line)
   - Bullet points or numbered list for multiple items
   - (blank line between sections)
   - A key takeaway or safety warning at the end if relevant
3. Technical standards and codes (e.g., OSHA 1910.132, ISO 45001:2018) must always be cited precisely.
4. Do not fabricate information — if context is insufficient say: "There is no sufficient information about this in the knowledge base."
5. If the user requests navigation to a page, include the action.

Output MUST be JSON only in this exact format:
{
  "reply": "Your structured reply here...\n\n• Point one\n• Point two\n\nKey takeaway...",
  "action": {"type": "navigate", "url": "/page"}
}
If no navigation:
{
  "reply": "Your structured reply here...",
  "action": null
}

Available pages:
- /index | /training | /training/courses | /first_aid | /fire_station | /safety_alerts | /admin | /Login

JSON only — no text before or after it."""


# ─── Core Logic ───────────────────────────────────────────────────────────────
def process_query(query: str) -> dict:

    # 1. Detect language → pick model + prompt
    lang = detect_language(query)
    if lang == "ar":
        model         = MODEL_ARABIC
        system_prompt = SYSTEM_PROMPT_AR
        error_msg     = "عذراً، حصلت مشكلة في الاتصال. ياريت تحاول تاني كمان شوية."
    else:
        model         = MODEL_ENGLISH
        system_prompt = SYSTEM_PROMPT_EN
        error_msg     = "Sorry, a connection error occurred. Please try again in a moment."

    # 2. Retrieve relevant documents from knowledge base
    docs    = retrieve_documents(query)
    context = "\n".join([doc.page_content for doc in docs])
    if not context.strip():
        context = "No specific information found in the knowledge base."

    # 3. Build prompt
    user_message = f"""Context from Knowledge Base:
{context}

User Query: {query}

Respond with a valid JSON object only."""

    messages = [
        {"role": "system", "content": system_prompt},
        {"role": "user",   "content": user_message},
    ]

    # 4. Call LLM
    try:
        client   = get_client(model)
        response = client.chat_completion(
            messages=messages,
            max_tokens=768,
            temperature=0.1,
        )

        response_text = response.choices[0].message.content.strip()

        # Extract JSON (model sometimes wraps in ```json ... ```)
        json_match = re.search(r'\{.*\}', response_text, re.DOTALL)
        if json_match:
            return json.loads(json_match.group(0))
        else:
            return {"reply": response_text, "action": None}

    except Exception as e:
        print(f"[LLM Error] {e}")
        return {"reply": error_msg, "action": None}
