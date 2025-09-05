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


## 6. JWT 설정
[JWT Key 제너레이터](https://jwtsecrets.com/)

### **application.yml** 설정
```aiexclude
  jwt:
    # https://jwtsecrets.com/
    secret: "발급 받은 키 번호를 입력해주세요"
    expiry:
      # ms -> 1/1000 -> 1초 -> 1000ms
      # 60 * 60
      access: 3600000
      # 24 * 60 * 60 = 86400000
      refresh: 86400000
```

### 🔑 **JwtUtil 개념 설명**
- JWT 발급 → 값 꺼내기 → 검증 기능을 모아놓은 유틸 클래스

#### 1) 클래스 개요

* **역할**: 사용자 로그인 성공 시 Access Token / Refresh Token을 발급
* `@Component`: 스프링 빈으로 등록 → 다른 서비스/필터에서 주입해서 사용 가능
* JWT의 생성과 관련된 로직만 담당 (순수 유틸 성격)

---

#### 2) 주요 필드

```java
private final SecretKey secretKey;   // JWT 서명용 비밀키
private final Long accessExpiry;     // Access Token 만료시간(ms)
private final Long refreshExpiry;    // Refresh Token 만료시간(ms)
```

* **SecretKey**

  * JWT는 "서명(signature)"을 포함해 위조 여부를 검증
  * `Keys.hmacShaKeyFor(secret.getBytes(...))` → HMAC-SHA 알고리즘 기반 SecretKey 생성
  * `application.yml`에서 `jwt.secret` 값을 가져옴
* **accessExpiry**

  * Access Token 만료 시간 (보통 수분\~수시간)
* **refreshExpiry**

  * Refresh Token 만료 시간 (보통 수일\~수주)

---

#### 3) 생성자

```java
public JwtUtil(
        @Value("${jwt.secret}") String secret,
        @Value("${jwt.expiry.access}") Long accessExpiry,
        @Value("${jwt.expiry.refresh}") Long refreshExpiry) {
    this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    this.accessExpiry = accessExpiry;
    this.refreshExpiry = refreshExpiry;
}
```

* `@Value` → `application.yml`에 작성한 설정값을 자동 주입
* 예시:

  ```yaml
  jwt:
    secret: "내JWT시크릿키1234567890...."
    expiry:
      access: 3600000   # 1시간 (ms)
      refresh: 1209600000 # 2주 (ms)
  ```

---

#### 4) 토큰 생성 메서드

```java
public String generateToken(String username, String role, boolean isRefresh)
```

* **매개변수**

  * `username`: 토큰 주인 (Subject)
  * `role`: 사용자 권한(Role) → Claim에 포함
  * `isRefresh`: Refresh Token 여부

    * `true` → Refresh Token 발급
    * `false` → Access Token 발급

---

#### 5) JWT 빌더

```java
return Jwts.builder()
        .subject(username)  // JWT의 Subject (토큰 주인)
        .claim("role", role) // 추가 정보(Claims)
        .issuedAt(new Date()) // 발급 시간
        .expiration(new Date(System.currentTimeMillis() + (isRefresh ? refreshExpiry : accessExpiry))) // 만료 시간
        .signWith(secretKey) // 서명(Signature)
        .compact(); // 최종 문자열 반환
```

* **subject**: 토큰의 주체 (보통 username)
* **claim**: 커스텀 데이터 추가 (role 등)
* **issuedAt**: 발급 시각
* **expiration**: 만료 시각
* **signWith**: SecretKey 기반으로 서명 → 위조 방지
* **compact()**: 최종 JWT 문자열 생성

---

### 🛠️ JWT 발급 흐름

1. 사용자가 로그인 요청 (`/auth/login`)
2. 서버에서 사용자 인증 성공 → `JwtUtil.generateToken()` 호출
3. Access Token & Refresh Token 발급
4. 클라이언트(브라우저/앱)가 Access Token을 요청 헤더에 넣어서 API 호출
5. 서버는 토큰 검증 후 요청 처리

---

### 🧩 Access Token vs Refresh Token

* **Access Token**

  * 짧은 유효 기간 (분\~시간)
  * 요청 시 `Authorization: Bearer {토큰}` 으로 포함
* **Refresh Token**

  * 긴 유효 기간 (일\~주)
  * Access Token이 만료되었을 때 새 Access Token 재발급용
  * 보통 DB/Redis에 저장해 관리

## 7. CustomUserDetailsService 생성

### 1. `@Service` + `@RequiredArgsConstructor`

* **서비스 빈 등록**: 스프링이 관리하는 서비스 계층 클래스
* `final UserAccountRepository`를 **생성자 주입**으로 자동 연결

---

### 2. `implements UserDetailsService`

* **Spring Security의 필수 인터페이스**
* 로그인 과정에서 **`loadUserByUsername()`** 메서드를 반드시 구현해야 함
* Security가 로그인 시 사용자 이름(username)으로 호출 → 여기서 DB에서 유저 검색

---

### 3. `loadUserByUsername(String username)`

```java
UserAccount userAccount = userAccountRepository.findByUsername(username)
    .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다"));
```

* DB에서 username으로 사용자 찾기
* 없으면 `UsernameNotFoundException` 발생 (Spring Security가 처리)

---

### 4. `User.builder()`

```java
return User.builder()
        .username(userAccount.getUsername())
        .password(userAccount.getPassword())
        .roles(userAccount.getRole())
        .build();
```

* 찾은 사용자를 **Spring Security 전용 User 객체**로 변환
* **username, password, role** 정보를 Security가 관리할 수 있게 전달

## 8. JWT 필터 생성
### 1. `extends OncePerRequestFilter`

* **매 요청마다 단 한 번 실행되는 필터**
* 클라이언트 요청이 들어올 때 `doFilterInternal()` 실행됨

---

### 2. 주요 필드

```java
private final JwtUtil jwtUtil;
private final UserDetailsService userDetailsService;
```

* **`JwtUtil`** : 토큰 생성·검증 유틸리티
* **`UserDetailsService`** : username으로 사용자 정보(DB) 조회

---

### 3. `doFilterInternal()` 동작 흐름

#### (1) 쿠키에서 토큰 찾기

```java
for (Cookie c : request.getCookies()) {
    if (c.getName().equals("access_token")) {
        token = c.getValue();
        break;
    }
}
```

* 요청에 포함된 쿠키 중 `"access_token"` 이름의 쿠키에서 JWT 추출

---

#### (2) 토큰이 없으면 → 그냥 다음 필터로 진행

```java
if(token == null) {
    filterChain.doFilter(request, response);
    return;
}
```

---

#### (3) 토큰이 유효하지 않으면 → 401 응답

```java
if(!jwtUtil.validateToken(token)) {
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
}
```

---

#### (4) 토큰이 유효하면 → 사용자 정보 로드

```java
String username = jwtUtil.getUsername(token);
UserDetails userDetails = userDetailsService.loadUserByUsername(username);
```

* 토큰 안의 username 추출
* DB에서 사용자 정보를 가져옴

---

#### (5) 인증 객체(Authentication) 생성 후 SecurityContext에 저장

```java
Authentication authentication =
    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

SecurityContextHolder.getContext().setAuthentication(authentication);
```

* 인증된 사용자 정보(Security에서 사용 가능한 객체) 생성
* **SecurityContext**에 등록 → 이후 컨트롤러에서 `@AuthenticationPrincipal` 같은 방식으로 접근 가능

---

#### (6) 다음 필터로 요청 전달

```java
filterChain.doFilter(request, response);
```