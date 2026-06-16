# TasteFilter 백엔드 시스템 (TasteFilter-Backend)
블로그 운영 중, 
과도한 협찬 광고 및 어뷰징 리뷰 데이터를 시스템 레이어에서 자동으로 필터링하여, 순도 높은 '내돈내산' 맛집 데이터를 수집 및 정제하고 제공하는 목적을 가진 프로젝트입니다.
REST API 서버입니다.

## 1. 프로젝트 목적 및 핵심 전략
* **데이터 무결성 확보:** 유저 요청 시점에 외부 플랫폼을 실시간으로 크롤링하는 방식 대신, 백그라운드 스케줄러(Batch)를 통해 주기적으로 데이터를 사전 수집 및 정제하여 자체 DB에 적재합니다. 이를 통해 유저 조회 시 발생할 수 있는 응답 지연을 방지하고, 외부 플랫폼의 IP 차단 위험을 최소화합니다.
* **다차원 필터링 파이프라인:** 텍스트 정규식 패턴 매칭, 하단 DOM 구조 분석(공정위 배너 및 스티커 이미지 식별), 리뷰 이벤트 패턴 판별을 통해 광고성 데이터를 시스템 단에서 완전 무력화합니다.

## 2. 타겟 상권 및 분류 체계
시스템 안정성과 필터링 변별력을 검증하기 위해 바이럴 마케팅이 가장 치열한 서울 핵심 상권을 중심으로 데이터를 수집합니다.
* **대상 지역:** 성수, 강남, 홍대, 종로/종각, 건대
* **음식 카테고리:** 한식, 일식, 중식, 양식, 아시아, 기타

## 3. 기술 스택
* **Language:** Java 21
* **Framework:** Spring Boot 4.1.0
* **Data Access:** Spring Data JPA
* **Database:** PostgreSQL 15
* **Data Collection:** Jsoup
* **Build Tool:** Gradle (Groovy)
* **Infra:** Docker, Docker Compose

---

## 4. 시작 가이드 (Getting Started)

본 프로젝트는 로컬 개발 환경의 일관성을 위해 Docker Compose를 활용하여 PostgreSQL 환경을 구성합니다. 보안을 위해 민감한 DB 접속 정보는 환경 변수로 분리하여 관리합니다.

### 4.1. 환경 변수 세팅
프로젝트 루트 디렉토리에 `.env` 파일을 생성하고 아래의 환경 변수를 환경에 맞게 입력해 주세요. (해당 파일은 `.gitignore`에 등록되어 깃허브에 커밋되지 않습니다.)

```env
# .env 예시
POSTGRES_USER=your_db_username
POSTGRES_PASSWORD=your_db_password
POSTGRES_DB=taste_filter