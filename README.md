
-----

# 👨‍💻 CoMentor (Backend)

<br>
<br>

**"내 코드에서 시작하는 진짜 면접 준비"** <br>
GitHub 커밋 로그를 분석하여 프로젝트 맞춤형 CS 질문을 생성해주는 <br>
**개발자 면접 대비 & 회고 플랫폼**입니다.

<br>

## 💡 Project Background

많은 취업 준비생들이 CS(Computer Science) 지식을 단순 암기하지만, 실제 면접에서는 "이 기술을 프로젝트에 어떻게 적용했나요?"라는 질문에 답변하지 못하는 문제에 주목했습니다.

| 구분 | 내용 |
| :---: | :--- |
| **🚨 Problem** | 기능 구현에만 몰두하여, 본인이 작성한 코드에 어떤 CS 개념이 적용되었는지 설명하지 못함. |
| **✅ Solution** | GitHub API와 LLM을 연동하여, 사용자의 **실제 커밋 내역을 분석하고 연관된 기술 면접 질문**을 자동 생성. |
| **🎯 Value** | **단순 암기 탈피**, 내 프로젝트 경험에 기반한 **논리적 답변 능력 향상**, 지속적인 **학습 루틴 형성**. |

<br>

## 🛠 Tech Stack

| Category | Technologies |
| :--- | :--- |
| **Language & Framework** |  **Java 17**<br> **Spring Boot** |
| **Database** |  **MySQL 8.0** |
| **AI & External API** |  **OpenAI API (GPT-3.5)**<br> **GitHub REST API** |
| **Infrastructure** |  **AWS**<br> **Docker** |
| **Security & Auth** |  **Spring Security**<br> **OAuth2 (GitHub)**<br> **JWT** |
| **Notification** | **Firebase Cloud Messaging (FCM)** |

<br>

## 🔑 Key Features

### 1\. 코드 기반 면접 질문 생성 (Code Analysis)

  - **GitHub 연동**: `GithubRepoService`를 통해 사용자의 레포지토리와 커밋 내역을 불러옵니다.
  - **코드 분석 파이프라인**: 사용자가 선택한 특정 기간/파일의 코드 변경 사항(Diff)을 추출합니다.
  - **AI 질문 생성**: `GptService`에서 추출된 코드를 기반으로 프롬프트를 최적화하여 전송, 해당 로직에 사용된 자료구조, 알고리즘, 디자인 패턴 등 심층 CS 질문을 생성합니다.

### 2\. 데일리 CS 학습 루틴 (Routine & Notification)

  - **스케줄러 시스템**: `DailyQuestionScheduler`를 통해 매일 오전 10시, 사용자가 설정한 기술 스택(Frontend, Backend 등)에 맞춰 새로운 CS 질문 4개를 자동 배정합니다.
  - **Push 알림**: 사용자가 앱을 켜지 않아도 학습 시점을 놓치지 않도록 FCM(Firebase)을 통해 '오늘의 질문 도착', '48시간 미학습 경고' 등의 알림을 발송합니다.

### 3\. 피드백 및 평가 (AI Feedback)

  - **서술형 채점**: 사용자가 작성한 답변을 OpenAI API로 전송하여 단순 정답 여부뿐만 아니라, 답변의 논리적 허점과 보완할 키워드를 포함한 상세 피드백을 제공합니다.
  - **아카이빙**: `FolderService`를 통해 사용자가 학습한 질문과 피드백을 폴더별로 분류하고 북마크하여 나만의 면접 노트를 구성할 수 있습니다.

### 4\. 학습 데이터 시각화 (Dashboard)

  - **학습 로그 추적**: `UserStudyLogService`를 통해 일별 학습량과 연속 학습일(Streak)을 기록합니다.
  - **취약점 분석**: 카테고리별(OS, DB, 네트워크 등) 정답률을 분석하여 사용자가 자주 틀리는 영역을 시각적으로 제공, 부족한 부분을 집중 공략할 수 있게 돕습니다.

<br>

## 📂 Project Structure

기능별 응집도를 높인 패키지 구조입니다.

```bash
com.knu.coment
├── config        # GPT, Firebase, Security, Swagger 설정
├── controller    # REST API 진입점 (User, Question, Project 등)
├── service       # 비즈니스 로직 (GitHub 파싱, GPT 통신, 스케줄링)
├── repository    # JPA DB 접근 계층
├── entity        # 도메인 엔티티 (User, Project, Question, Answer)
├── dto           # 데이터 전송 객체 (Request/Response)
├── security      # JWT 인증 필터 및 핸들러
├── global        # 공통 코드 (Enum, Response Format)
└── exception     # 전역 예외 처리 (GlobalExceptionHandler)
```
