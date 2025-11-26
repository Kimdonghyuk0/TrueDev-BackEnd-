from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
import json
import ollama
import logging


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
    try:
        parsed = json.loads(content)
        is_fact = bool(parsed.get("isFact"))
        ai_comment = parsed.get("aiComment", "")
    except Exception:
        # LLM이 JSON을 지키지 못한 경우 보수적으로 false 처리
        is_fact = False
        ai_comment = content or "사실 여부를 판별하지 못했습니다."

    # aiComment가 비어있지 않으면 무조건 false로 간주
    if ai_comment and str(ai_comment).strip():
        is_fact = False

    return FactResponse(isFact=is_fact, aiComment=ai_comment)
