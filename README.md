# **게시판 프로젝트 (개념 정리, Spring Security + JWT 포함)**

---

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

> 🔹 민감 정보는 `.gitignore`에 추가 (`*-dev.*`)
> 🔹 JWT 의존성은 `build.gradle`에 추가

---

## **1. 프로젝트 설정**

* **application.yml / application-dev.yml**: 환경별 DB, JPA, JWT 설정
* JPA를 통해 자바 객체(Entity)와 DB 매핑
* 서버 실행 → DB 연결 및 서버 정상 동작 확인

---

## **2. 페이지 연결**

### **Controller**

* `@Controller` → View 반환용
* `@GetMapping` → GET 요청 처리
* `@RequestMapping("/auth")` → URL prefix 지정 (`/auth/**`)

### **템플릿**

* Thymeleaf 사용 → `th:action`, `th:value` 등 활용
* 회원가입 폼 예:

```html
<form th:action="@{/auth/register}" method="post">
```

### **실행 환경**

* IDE에서 실행 → 브라우저 자동 시작 가능

---

## **3. Spring Security 설정**

### **SecurityFilterChain**

* CSRF, 기본 로그인 페이지, HTTP Basic 인증 비활성화
* `/`와 `/auth/register` 접근 허용
* 그 외 페이지는 로그인 필요

### **PasswordEncoder**

* 비밀번호 암호화 (`bcrypt`)
* 로그인 시 입력 비밀번호와 DB 저장 비밀번호 비교

### **AuthenticationManager**

* 인증 처리 담당
* SecurityFilterChain과 함께 로그인, 권한 검증 수행

### **인증 흐름**

1. 클라이언트 요청 → SecurityFilterChain
2. URL 접근 권한 체크
3. 로그인 요청 → AuthenticationManager → PasswordEncoder로 비밀번호 검증
4. 인증 성공 → Controller 처리

---

## **4. JPA 설정 (UserAccount)**

* 엔티티 필드

| 필드       | 역할     | 제약                       |
| -------- | ------ | ------------------------ |
| id       | PK     | auto increment           |
| username | 로그인 ID | unique, not null, max 50 |
| password | 비밀번호   | not null                 |
| role     | 권한     | not null, max 20         |

* Lombok: `@Getter`, `@Setter`
* Spring Security 연계 가능

---

## **5. 회원가입 기능**

### **Repository**

* `UserAccountRepository` → CRUD + `findByUsername()`

### **Service**

* 사용자 등록, 비밀번호 암호화, 중복 체크, DB 저장
* `@Transactional` 적용

### **Controller**

* `/auth/register` GET → 폼 페이지
* `/auth/register` POST → Service 호출 후 DB 저장
* 에러 시 `RedirectAttributes`로 메시지 전달

### **View**

* `register.html` → 사용자 입력 → Controller POST 전달

---

## **6. JWT 설정**

### **application.yml**

```yaml
jwt:
  secret: "JWT_시크릿키_입력"
  expiry:
    access: 3600000      # 1시간
    refresh: 86400000    # 1일
```

### **JwtUtil 역할**

1. JWT 발급
2. JWT 데이터 추출 (username, role)
3. JWT 검증

### **주요 메서드**

* `generateToken(username, role, isRefresh)` → JWT 발급
* `getUsername(token)` → 토큰에서 username 추출
* `getRole(token)` → 토큰에서 role 추출
* `validateToken(token)` → 토큰 유효성 체크

> 🔑 Access Token: 짧은 만료 시간
> 🔑 Refresh Token: 긴 만료 시간, Access Token 재발급용

---

## **7. CustomUserDetailsService**

* `UserDetailsService` 구현 필수
* `loadUserByUsername(username)` → DB 조회 후 Spring Security용 `User` 반환
* 인증 성공 시 SecurityContext에 등록 가능
* `User.builder().username(...).password(...).roles(...)`

> ⚠️ `roles()`에 `ROLE_` 자동 추가 → `"ROLE_USER"`처럼 직접 `"ROLE_"` 붙이면 안됨

---

## **8. JWT 필터 (JwtFilter)**

### **역할**

* 요청마다 Access Token 검증
* 유효하면 SecurityContext에 인증 정보 등록

### **동작 흐름**

1. 쿠키에서 `access_token` 추출
2. 없으면 필터 체인 통과
3. 토큰 유효 → username 추출 → UserDetails 조회 → SecurityContextHolder에 등록
4. 토큰 만료/유효하지 않으면 401 응답
5. 다음 필터로 진행

### **SecurityConfig에서 필터 등록**

```java
http.addFilterBefore(new JwtFilter(jwtUtil, userDetailsService),
        UsernamePasswordAuthenticationFilter.class);
```

* 기본 Username/Password 필터 전에 JWT 필터 실행

---

## **9. 로그인 처리 (AuthController)**

* `/auth/login` GET → 로그인 폼
* `/auth/login` POST → 로그인 처리

  1. AuthenticationManager → 아이디/비번 검증
  2. JwtUtil → JWT 발급
  3. ResponseCookie → HttpOnly 쿠키에 JWT 전달
  4. 성공 → `/my-page` 이동
  5. 실패 → 에러 메시지 담아 `/auth/login` 재전송

---

## **10. 로그인 후 사용자 페이지 (/my-page)**

```java
@GetMapping("/my-page")
public String myPage(Model model, Authentication authentication) {
    model.addAttribute("username", authentication.getName());
    return "my-page";
}
```

* SecurityContext에서 인증 정보 가져오기
* 로그인한 사용자 이름을 뷰에 전달

---

## **11. Refresh Token & 재발급 필터**

### **RefreshTokenReissueFilter 역할**

1. 요청 쿠키에서 Access Token 확인
2. 정상 → SecurityContext에 인증 정보 등록
3. 만료 → Refresh Token 확인
4. Refresh Token 정상 → 새 Access Token 발급 → SecurityContext 등록
5. Refresh Token 문제 → 인증 실패 처리
6. 필터 체인 계속 진행

### **필드**

```java
private final JwtUtil jwtUtil;
private final UserDetailsService userDetailsService;
private final RefreshTokenRepository refreshTokenRepository;
```

### **동작 요약**

* Access Token 유효 → 인증 처리
* Access Token 만료 → Refresh Token 검증 → 새 Access Token 발급
* SecurityContextHolder에 인증 정보 등록
* 최종적으로 filterChain.doFilter() 호출하여 요청 전달

---

## **12. 전체 인증 흐름 정리**

1. 클라이언트 요청 → JWT 필터 실행
2. Access Token 검증

  * 정상 → SecurityContext 등록 → Controller로 전달
  * 만료 → Refresh Token 검증

    * 정상 → 새 Access Token 발급 → SecurityContext 등록
    * 실패 → 401/인증 실패 처리
3. SecurityContextHolder에 인증 정보 등록 후 Controller 실행
4. Controller에서 인증 정보 사용 → 사용자 이름/권한 등 표시 가능