from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
import json
import ollama
import logging
import re


class FactRequest(BaseModel):
    text: str


class FactResponse(BaseModel):
    isFact: bool
    aiComment: str


app = FastAPI(title="TrueDev Fact Checker", version="0.1.0")

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("fact-checker")

SYSTEM_PROMPT = """\
You are a concise fact-checker. Given a user's statement, decide if it is factually correct.
Return a strict JSON object with:
- "isFact": true or false
- "aiComment": brief Korean feedback (empty string if isFact is true).

- Set "isFact": false ONLY when you clearly detect a contradiction or a widely known false claim.
- If uncertain/ambiguous/주관적이면 isFact: true 로 두고 aiComment는 빈 문자열로.
- Return strict JSON: {"isFact": true/false, "aiComment": "<짧은 한글 피드백 또는 빈 문자열>"}.
Output ONLY the JSON.
If information is insufficient, treat it as not certain and set isFact to false with a short reason.
Output ONLY the JSON.
"""

def _strip_fence(text: str) -> str:
    if not text:
        return ""
    t = text.strip()
    if t.startswith("```"):
        t = re.sub(r"^```[a-zA-Z0-9]*\s*", "", t, flags=re.MULTILINE)
        t = re.sub(r"```$", "", t, flags=re.MULTILINE)
    return t.strip()

def _parse_llm_content(raw: str) -> FactResponse:
    cleaned = _strip_fence(raw)
    # 1) JSON 시도
    try:
        parsed = json.loads(cleaned)
        is_fact = bool(parsed.get("isFact"))
        ai_comment = parsed.get("aiComment", "")
        return FactResponse(isFact=is_fact, aiComment=str(ai_comment))
    except Exception:
        pass

    # 2) 단순 패턴 매칭 (isFact true/false가 텍스트에 있을 때)
    is_fact = False
    if '"isFact": true' in cleaned.lower() or "isfact: true" in cleaned.lower():
        is_fact = True
    if '"isFact": false' in cleaned.lower() or "isfact: false" in cleaned.lower():
        is_fact = False

    # 3) JSON이 아닐 경우, 전체 텍스트를 코멘트로
    ai_comment = cleaned
    return FactResponse(isFact=is_fact, aiComment=ai_comment)


@app.post("/fact-check", response_model=FactResponse)
async def fact_check(req: FactRequest):
    prompt = f"사용자가 제시한 문장:\n\"\"\"\n{req.text}\n\"\"\"\n위 문장이 사실이면 isFact=true, 아니면 false와 함께 짧은 한국어 피드백(aiComment)을 JSON으로 반환하세요."
    try:
        result = ollama.chat(
            model="gemma3:4b",  # 로컬에 존재하는 4B 모델 사용
            messages=[
                {"role": "system", "content": SYSTEM_PROMPT},
                {"role": "user", "content": prompt},
            ],
            options={"temperature": 0.4},
        )
    except Exception as exc:
        raise HTTPException(status_code=500, detail=f"Ollama 호출 실패: {exc}")

    content = result.get("message", {}).get("content", "").strip()
    logger.info("LLM raw response: %s", content)

    parsed = _parse_llm_content(content)

    # 정책: 코멘트가 비어있지 않으면 실패로 본다 (경고/피드백)
    if parsed.aiComment and str(parsed.aiComment).strip():
        parsed.isFact = False

    return parsed
