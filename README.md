# **게시판 프로젝트 (개념 정리)**

## **0. 프로젝트 의존성 구성**

| 의존성                  | 용도              | 주요 기능                                   |
| -------------------- | --------------- | --------------------------------------- |
| Spring Boot DevTools | 개발 편의성          | 코드 변경 시 자동 재시작, 브라우저 LiveReload         |
| Lombok               | 코드 간결화          | Getter/Setter, Builder 등 반복 코드 자동 생성    |
| Spring Web           | 웹 개발            | REST API 구현, 내장 Tomcat 서버 사용            |
| Thymeleaf            | 템플릿 엔진          | 서버 사이드 HTML 렌더링, 브라우저에서 템플릿 확인 가능       |
| Spring Data JPA      | SQL 데이터 연동      | JPA 기반 데이터 접근, Hibernate 사용             |
| MySQL Driver         | MySQL 연동        | JDBC를 통한 MySQL 접근                       |
| Spring Security      | 인증/권한 관리        | 로그인, 권한 체크 등 보안 기능 제공                   |
| Spring Data Redis    | NoSQL(Redis) 연동 | 캐시, 세션 관리, Cluster/Sentinel/Reactive 지원 |
| JWT                  | 인증 토큰           | 토큰 기반 인증 구현용                            |

* `build.gradle`에 JWT 의존성 추가
* 민감 정보는 `.gitignore`에 추가 (`*-dev.*`)

---

## **1. 프로젝트 설정**

* **application.yml / application-dev.yml**로 환경 구성

  * DB 연결 정보, Hibernate 설정 등
  * JPA를 통해 **자바 객체(Entity)로 DB 조작**
* **main 함수 실행** → DB 연결 및 서버 정상 확인

---

## **2. 페이지 연결**

### **Controller**

* `@Controller`: View 반환, 클래스가 컨트롤러임 표시
* `@GetMapping`: GET 요청 처리
* `@RequestMapping("/auth")`: URL prefix 지정 (`/auth/**`)

### **템플릿**

* Thymeleaf 사용 (`th:` 속성 활용)
* HTML 구조: `lang="ko"` → 한국어, SEO/접근성 향상
* 회원가입 폼: `<form th:action="@{/auth/register}" method="post">` → Controller로 POST 전송

### **실행 환경**

* IDE 설정으로 프로젝트 실행 시 브라우저 자동 시작

---

## **3. 보안 설정 (Spring Security)**

* **SecurityFilterChain**

  * CSRF, 기본 로그인 페이지, HTTP Basic 인증 비활성화
  * `/`와 `/auth/register`는 모두 접근 허용
  * 그 외 페이지는 로그인 필요

* **PasswordEncoder**

  * 비밀번호 암호화 처리 (`bcrypt` 기본)
  * 로그인 시 입력 비밀번호와 DB 저장 비밀번호 비교

* **AuthenticationManager**

  * 인증 처리 담당
  * SecurityFilterChain과 함께 로그인, 권한 검증 수행

* **전체 흐름**

  1. 클라이언트 요청 → SecurityFilterChain
  2. 요청 URL 접근 권한 체크
  3. 로그인 요청 → AuthenticationManager → PasswordEncoder로 비밀번호 검증
  4. 인증 성공 → 컨트롤러로 요청 전달

---

## **4. JPA 설정**

* **UserAccount 엔티티**

  * JPA 엔티티: DB 테이블과 매핑
  * 필드

    * `id`: Primary Key, 자동 증가
    * `username`: 로그인 ID, 중복 불가, NULL 불가, 최대 50자
    * `password`: 비밀번호, NULL 불가
    * `role`: 권한, NULL 불가, 최대 20자
  * 컬럼 제약: `nullable`, `unique`, `length`
  * Lombok 사용: `@Getter`, `@Setter`
  * Spring Security 연계: 인증/권한 관리 가능

---

## **5. 회원가입 기능**

### **Repository**

* `UserAccountRepository`

  * CRUD 기능 제공, 복잡한 SQL 없이 DB 처리 가능
  * 커스텀 메서드: `findByUsername()` → 메서드 이름 기반 쿼리 생성

### **Service**

* `UserAccountService`

  * 사용자 등록 로직 처리
  * 비밀번호 암호화, 중복 체크, DB 저장
  * `@Transactional` → 트랜잭션 보장

### **Controller**

* `AuthController`

  * 회원가입 페이지 표시 (`GET /auth/register`)
  * 회원가입 처리 (`POST /auth/register`)
  * Controller → Service → Repository → DB 흐름 수행

### **View**

* 회원가입 폼 (`register.html`)

  * 사용자 입력 받아 컨트롤러 전달
  * Thymeleaf로 Spring MVC Controller와 동적 연동

---

### **정리**

1. 프로젝트 실행 → DB 연결 확인
2. Controller → Service → Repository → DB → View
3. Security 설정으로 URL 접근 권한 관리
4. JPA 엔티티로 객체 지향적 DB 처리
5. 회원가입, 로그인, 권한 체크 기능 구현 가능
