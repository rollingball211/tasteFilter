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
NAVER_CLIENT_ID=your_naver_client_id
NAVER_CLIENT_SECRET=your_naver_client_secret
```

## 5. 개발 진행 기록

### 2026-06-22

* 네이버 개발자센터에서 검색 API 애플리케이션을 등록했습니다.
* `NAVER_CLIENT_ID`, `NAVER_CLIENT_SECRET`을 환경변수로 분리했습니다.
* 네이버 블로그 검색 API에 `성수 한식 맛집`을 요청해 HTTP 200 응답과 검색 결과 반환을 확인했습니다.

### 2026-06-25

* 네이버 블로그 검색 API 연동 코드를 추가하고, 검색 결과를 `CrawledReview` 후보 데이터로 변환하도록 구성했습니다.
* 검색 API는 본문 전체를 제공하지 않으므로, Jsoup 기반 `NaverBlogPostParser`를 추가해 네이버 블로그 상세 본문을 가져오도록 했습니다.
* 블로그 상세 페이지의 `mainFrame`, `se-main-container`, `postViewArea` 구조를 확인하고 본문 추출 기준을 정리했습니다.
* `ReviewFilterService`를 통해 협찬/광고/체험단/리뷰 이벤트 문구를 판별하고, 통과한 리뷰만 저장하도록 서비스 흐름을 구성했습니다.
* `NaverBlogCrawlOrchestrator`를 추가해 검색 후보 조회, 중복 URL 스킵, 상세 파싱, 필터링, 저장 결과 집계를 한 번에 실행할 수 있게 했습니다.
* 크롤링 실행 결과를 확인하기 위해 `CrawlCandidateSearchResult`, `CrawlIngestionResult` DTO를 추가했습니다.

### 2026-06-27

* 특정 식당의 크롤링 파이프라인을 직접 실행하는 관리자용 수동 API를 추가했습니다.
* 식당 ID를 받아 `NaverBlogCrawlOrchestrator`를 호출하고 후보, 중복, 저장, 거절, 실패 건수를 반환하도록 구성했습니다.
* 검색어에 식당명을 포함해 같은 지역과 음식 카테고리의 다른 식당 리뷰가 섞일 가능성을 낮췄습니다.
* 존재하지 않는 식당 ID는 `404 Not Found`로 응답하도록 관리자 API 예외 처리를 추가했습니다.
* 현재 관리자 인증은 아직 적용하지 않았으므로 수동 크롤링 API는 로컬 검증용으로 사용합니다.

### 2026-06-28

* 식당별 저장 리뷰를 최신순으로 조회하는 `GET /api/restaurants/{restaurantId}/reviews` API를 추가했습니다.
* 리뷰가 많아져도 한 번에 모두 읽지 않도록 `page`, `size` 기반 페이지 조회를 적용했습니다.
* JPA Entity를 직접 응답하지 않고 `ReviewResponse`, `RestaurantReviewsResponse` DTO로 API 구조를 분리했습니다.
* 존재하지 않는 식당 ID의 `404 Not Found` 처리를 관리자 API뿐 아니라 전체 API에서 공통 사용하도록 확장했습니다.
