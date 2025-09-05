# **게시판 프로젝트**

## **0. 프로젝트 의존성 구성**

### **1) 프로젝트 생성**

* 사이트: [start.spring.io](https://start.spring.io)

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

---

### **2) 추가 의존성**

* `build.gradle` 파일에 **JWT** 의존성 추가

```gradle
// 웹토큰 의존성 추가
implementation 'io.jsonwebtoken:jjwt-api:0.13.0'
runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.13.0'
runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.13.0'
```

---

### **3) application.properties → application.yml 변경**

```yaml
spring:
  application:
    name: my-board

  # 개발 모드로 설정
  profiles:
    active: dev

  # 공통사항
  # JPA 개념 다시 보기
  # 자바에서 데이터베이스를 객체 지향적으로 다루기 위한 표준 인터페이스
  # SQL을 직접 작성하지 않고도 자바 객체(Entity)를 통해 DB를 조작할 수 있음
  datasource: # JPA
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    show-sql: true
```

---

### **4) 주의 사항**

* 민감 정보가 포함된 파일(.env, *-dev.* 등)은 **GitHub 업로드 금지**
* `.gitignore`에 추가

```gitignore
### 개발용 민감 파일 제외
*-dev.*
```

### 5) application-dev.yml 작성
- DB 연결 정보, Hibernate 설정 등 개발용 환경 설정
- 예시 ([Aiven MySQL](aiven.io) 기준)
```yaml
spring:
  datasource:
    url: "본인_aiven_mysql_database_URL"
    username: "본인_aiven_mysql_계정"
    password: "본인_aiven_mysql_패스워드"

  jpa:
    hibernate:
      ddl-auto: update
```
