# TrueDev Backend 

**TrueDev**는 “가짜 정보 없는 커뮤니티”를 목표로 하는 서비스의 백엔드 모듈입니다.  
Spring Boot 기반 REST API와 로컬 LLM(FastAPI + Ollama gemma4b)을 연동해, 게시글과 댓글을 AI로 검증합니다.

---

## 🎥 Demo Video

[![Demo Video](https://img.youtube.com/vi/cdkhMjvxTyM/0.jpg)](https://youtu.be/cdkhMjvxTyM?si=Bd4gksLox7sQPeRb)

---

## 시스템 아키텍쳐

<img width="1260" height="891" alt="TrueDev시스템아키텍쳐" src="https://github.com/user-attachments/assets/2cee666b-35fa-446a-be8c-6811a806e032" />

---

## 1. 프로젝트 한 줄 소개

> 로컬 LLM 기반 AI 검증을 통해, 신뢰할 수 있는 정보를 구분할 수 있도록 설계된 커뮤니티

---

## 2. 주요 기능 요약

### 🧑‍💻 회원 & 인증
- 이메일/비밀번호 회원가입, 로그인, 로그아웃
- 프로필 관리
  - 닉네임, 이메일 수정
  - 프로필 이미지 업로드(Cloudinary)
- 비밀번호 변경 (BCrypt)
- JWT 기반 인증
  - Access Token + Refresh Token 발급
  - Refresh Token은 Redis에 저장 및 재발급 사용
  - Spring Security Filter 기반 인증/인가 처리

### 📝 게시글 & 댓글
- 게시글 CRUD (생성/조회/수정/삭제)
- 게시글 이미지 첨부 (Cloudinary 업로드 후 URL 저장)
- 조회수/좋아요/댓글 수 집계
- 댓글 CRUD 및 Soft Delete

### 🤖 AI 검증 (로컬 LLM)
- 게시글 내용 검증 API
  - FastAPI 서버에 텍스트 전달 → Ollama gemma4b 호출
  - 응답을 Article 엔티티에 저장
- 검증 상태 관리
  - `isVerified` : 검증 통과 여부
  - `isCheck` : 검증 응답 수신 여부
  - `aiMessage` : LLM 피드백 메시지
- 통계 API
  - 검증 통과 / 검증 대기 / 검증 실패 / 전체 게시글 수 집계

### 🧾 마이페이지
- 내가 쓴 글 목록 페이징 조회
- 내가 쓴 댓글 목록 페이징 조회
- 프로필/계정 정보 조회

---

## 3. 기술 스택

| 영역             | 기술                                                                 |
|------------------|----------------------------------------------------------------------|
| 언어             | Java 17                                                              |
| 프레임워크       | Spring Boot, Spring Security                                         |
| ORM              | Spring Data JPA, Hibernate                                           |
| 데이터베이스     | MySQL                                                                |
| 캐시/토큰 저장   | Redis (Refresh Token, 인증 관련 데이터)                             |
| AI 검증 서버     | FastAPI (Python), Ollama, gemma4b(로컬 LLM)                         |
| 이미지 업로드    | Cloudinary                                                           |
| 빌드/관리        | Gradle                                                               |
| 로깅             | LoggingInterceptor(AOP 유사 역할), Spring Logging, Hibernate SQL 로그 |

> FastAPI + Ollama 서버는 별도의 Python 환경에서 실행됩니다.

---

## 4. API

<img width="813" height="768" alt="image" src="https://github.com/user-attachments/assets/e08ed44d-669f-4002-83a6-043a719a2fec" />



---

## 5. 폴더 구조 

```bash
trueDev/
├─ build.gradle
├─ settings.gradle
├─ src
│  ├─ main
│  │  ├─ java/com/kdh/truedev/
│  │  │   ├─ article/            # 게시글 도메인 (entity, controller, service, repository, mapper)
│  │  │   ├─ comment/            # 댓글 도메인
│  │  │   ├─ user/               # 회원, 인증/인가, DTO, 서비스
│  │  │   ├─ redis/              # Redis 설정, RefreshToken 엔티티/리포지토리/유틸
│  │  │   ├─ config/             # CORS, Cloudinary, LoggingInterceptor 등 공통 설정
│  │  │   └─ springSecurity/     # JWT TokenProvider, Filter, SecurityConfig
│  │  └─ resources/
│  │      ├─ application.yml     # DB/Redis/LLM/FastAPI, Cloudinary 설정
│  │      └─ logback.xml (선택)  # 로깅 설정
│  └─ test/java/...              # 테스트 코드
└─ README.md                     # (이 문서)

