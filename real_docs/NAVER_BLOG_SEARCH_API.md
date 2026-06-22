# 네이버 블로그 검색 API 적용 가이드

## 문서 정보

- 공식 문서: https://developers.naver.com/docs/serviceapi/search/blog/blog.md
- 확인일: 2026-06-21
- 적용 프로젝트: TasteFilter-Backend

이 문서는 네이버 공식 블로그 검색 API를 TasteFilter의 리뷰 수집 파이프라인에 적용하기 위해 필요한 내용만 정리한다.

## 핵심 결론

네이버 블로그 검색 API는 검색어에 맞는 블로그 게시글의 제목, URL, 요약, 작성일 등을 반환한다. 게시글 전체 본문, 네이버 플레이스 ID, 영수증 인증 여부, 광고 배너 판정 결과는 제공하지 않는다.

따라서 이 프로젝트에서는 다음과 같이 사용한다.

1. 검색 API로 블로그 URL 후보를 수집한다.
2. `blogUrl` 중복 여부를 먼저 확인한다.
3. 접근이 허용된 게시글만 별도 파서로 분석한다.
4. 분석 결과를 `CrawledReview`로 변환한다.
5. `ReviewFilterService`로 광고 여부와 신뢰도 점수를 계산한다.
6. 필터를 통과한 리뷰만 DB에 저장한다.

## 사전 준비

네이버 개발자 센터에서 애플리케이션을 등록하고 사용 API에 `검색`을 추가해야 한다.

발급받은 값은 코드나 Git에 직접 기록하지 않고 환경변수로 관리한다.

```env
NAVER_CLIENT_ID=발급받은_클라이언트_아이디
NAVER_CLIENT_SECRET=발급받은_클라이언트_시크릿
```

API는 비로그인 오픈 API이며, OAuth 로그인 대신 요청 헤더의 클라이언트 아이디와 시크릿으로 인증한다.

## 요청 명세

### Endpoint

```text
GET https://openapi.naver.com/v1/search/blog.json
```

XML 응답이 필요하면 다음 Endpoint를 사용할 수 있지만, 이 프로젝트에서는 JSON을 사용한다.

```text
GET https://openapi.naver.com/v1/search/blog.xml
```

### 인증 헤더

```http
X-Naver-Client-Id: ${NAVER_CLIENT_ID}
X-Naver-Client-Secret: ${NAVER_CLIENT_SECRET}
```

### Query Parameters

| 이름 | 필수 | 범위/기본값 | 설명 |
| --- | --- | --- | --- |
| `query` | 필수 | UTF-8 인코딩 | 검색어 |
| `display` | 선택 | 기본 10, 최대 100 | 한 번에 받을 결과 개수 |
| `start` | 선택 | 기본 1, 최대 1000 | 검색 시작 위치 |
| `sort` | 선택 | `sim` 또는 `date` | 정확도순 또는 날짜순 |

TasteFilter의 정기 수집에서는 신규 게시글을 우선 확인할 수 있도록 `sort=date`를 기본으로 고려한다. 검색 품질 비교가 필요하면 `sim` 결과와 별도로 검증한다.

### 요청 예시

```http
GET /v1/search/blog.json?query=성수%20한식%20맛집&display=100&start=1&sort=date HTTP/1.1
Host: openapi.naver.com
X-Naver-Client-Id: ${NAVER_CLIENT_ID}
X-Naver-Client-Secret: ${NAVER_CLIENT_SECRET}
```

## 응답 명세

상위 응답에서 사용할 주요 필드는 다음과 같다.

| 필드 | 타입 | 용도 |
| --- | --- | --- |
| `lastBuildDate` | DateTime | 검색 결과 생성 시각 |
| `total` | Integer | 전체 검색 결과 수 |
| `start` | Integer | 현재 검색 시작 위치 |
| `display` | Integer | 현재 응답 결과 개수 |
| `items` | Array | 개별 블로그 검색 결과 |

`items`의 주요 필드는 다음과 같다.

| 필드 | 타입 | 프로젝트 사용 방식 |
| --- | --- | --- |
| `title` | String | 검색 결과 제목 |
| `link` | String | `blogUrl` 후보 및 중복 검사 기준 |
| `description` | String | 본문 파싱 실패 시 사용할 제한적인 요약 후보 |
| `bloggername` | String | 수집 로그 또는 분석용 메타데이터 |
| `bloggerlink` | String | 블로그 출처 메타데이터 |
| `postdate` | Date | 게시일 필터링 및 증분 수집 기준 |

`title`과 `description`에는 검색어가 `<b>` 태그로 감싸져 올 수 있다. 저장 또는 필터링 전에 HTML 태그를 제거해야 한다.

## 호출 한도와 페이징

- 공식 문서 기준 검색 API 호출 한도는 클라이언트 아이디별 하루 25,000회다.
- `display`는 최대 100이다.
- `start`는 최대 1000이다.
- 검색어별로 무제한 전체 결과를 순회할 수 없으므로 최근 게시물 중심의 증분 수집이 필요하다.

초기 운영 권장값:

```text
display=100
start=1
sort=date
```

지역과 음식 카테고리 조합별로 1페이지부터 수집하고, 마지막 수집일보다 오래된 게시물이 나오면 해당 검색어의 수집을 중단한다.

## 오류 처리

| 코드 | HTTP | 의미 | 처리 방향 |
| --- | --- | --- | --- |
| `SE01` | 400 | 잘못된 요청 | URL과 파라미터 확인 |
| `SE02` | 400 | 잘못된 `display` | 1~100 범위 확인 |
| `SE03` | 400 | 잘못된 `start` | 1~1000 범위 확인 |
| `SE04` | 400 | 잘못된 `sort` | `sim`, `date` 확인 |
| `SE06` | 400 | 잘못된 인코딩 | 검색어 UTF-8 인코딩 확인 |
| `SE05` | 404 | 존재하지 않는 API | Endpoint 오타 확인 |
| `SE99` | 500 | 네이버 내부 오류 | 제한된 횟수로 재시도 |

403이 발생하면 네이버 개발자 센터의 애플리케이션 API 설정에서 `검색` API가 활성화되어 있는지 확인한다. 인증정보 오류와 호출 한도 초과도 별도로 로그에 남긴다.

## TasteFilter 구현 구조

```text
NaverBlogSearchClient
  -> NaverBlogSearchResponse
  -> 중복 URL 사전 검사
  -> BlogPostParser
  -> CrawledReview
  -> ReviewFilterService
  -> ReviewService.saveFiltered()
  -> ReviewRepository
```

### `NaverBlogSearchClient`

- 네이버 검색 API 호출만 담당한다.
- 인증 헤더와 검색 파라미터를 구성한다.
- 응답 JSON을 DTO로 변환한다.
- DB 저장이나 광고 판정은 담당하지 않는다.

### `BlogPostParser`

- 허용된 게시글의 HTML에서 본문과 광고 신호를 추출한다.
- 검색 API의 `description`은 게시글 전문이 아니므로 정밀 필터링에 그대로 의존하지 않는다.
- HTML 구조 변경에 대비해 API 클라이언트와 별도 클래스로 유지한다.

### `ReviewService`

- `blogUrl` 중복을 확인한다.
- 필터를 통과한 리뷰만 저장한다.
- 검색 API 호출과 HTML 파싱 책임을 갖지 않는다.

## 검색어 구성

기본 조합은 지역, 음식 카테고리, 목적 키워드로 만든다.

```text
성수 한식 맛집
강남 일식 내돈내산
홍대 중식 추천
종로 양식 맛집
건대 아시아 음식 맛집
```

`내돈내산` 키워드만으로 신뢰성을 확정하면 안 된다. 광고 게시물도 해당 표현을 사용할 수 있으므로 검색 후보를 좁히는 용도로만 사용하고 최종 판정은 필터 서비스가 담당한다.

## API만으로 해결되지 않는 항목

- 게시글 전체 본문
- 이미지와 공정위 배너 분석
- 영수증 인증 여부
- 네이버 플레이스 ID
- 식당명과 게시글의 정확한 연결
- 인스타그램 해시태그 수

이 항목은 별도 HTML 파서, 이미지 분석, 장소 데이터 매핑 또는 다른 공식 API가 필요하다.

## 보안 및 운영 원칙

- 클라이언트 시크릿을 소스 코드와 Git에 저장하지 않는다.
- 로그에 인증 헤더를 출력하지 않는다.
- 실패 요청을 무한 재시도하지 않는다.
- 검색 결과 URL을 요청하기 전에 중복 여부를 확인한다.
- 게시글 전문을 그대로 DB에 보관하지 않고 필요한 요약과 판정 정보만 저장한다.
- 대상 사이트의 이용약관, robots 정책, 저작권 및 개인정보 관련 기준을 확인한다.

## 다음 구현 순서

1. 환경변수를 읽는 네이버 API 설정 클래스 작성
2. 검색 요청/응답 DTO 작성
3. Spring `RestClient` 기반 `NaverBlogSearchClient` 작성
4. 지역/카테고리 검색어 생성기 작성
5. 검색 결과의 HTML 태그 정리 및 URL 정규화
6. `BlogPostParser`와 Jsoup 의존성 추가
7. 전체 흐름을 묶는 `CrawlOrchestrator` 작성
8. 스케줄러와 호출량 제한 추가

