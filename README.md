# Vintly
빈티지 커뮤니티 백엔드 API 프로젝트입니다.

---
### :cd: 기술스택
- JAVA 17
- Spring Boot 3.4.0
- DB : MariaDB 11
- ORM : Spring Data JPA
- lib
  - validation
  - lombok
  - security + JWT

---
### 📝 API 명세서
- [명세서 바로가기](https://docs.google.com/spreadsheets/d/1ssHpxpQ8xyZtsD5vlBDZXf7hShBUGaIlTmG8yoGNku8/edit?gid=0#gid=0)

---
### 개선 사항
- 로그인 API 속도 개선 
  - [평균] 122ms > 109ms (mariadb > redis 적용) 약 1.12배
- 로그아웃 API 속도 개선
  - [평균] 32ms > 18ms (mariadb > redis 적용) 약 1.78배
- 토큰 재발급 API 속도 개선
  - [평균] 38ms > 20ms (mariadb > redis 적용) 약 1.9배
---
#### 사용 아이콘
- 이메일 : 📧
- error : 🔴