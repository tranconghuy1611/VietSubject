# 📘 Smart Revision & Exam Platform

Hệ thống EdTech giúp học sinh tiểu học (lớp 1–5) luyện tập và thi thử với cơ chế học thích ứng (adaptive learning).

---

## 🚀 Chức năng chính

### 🔐 Authentication

* JWT + Refresh Token
* Google OAuth2
* Role: `STUDENT`, `PARENT`, `ADMIN`

---

### 📚 Practice (Luyện tập)

* Tạo session → làm bài → kết thúc
* Adaptive difficulty:

  * WEAK → EASY
  * MEDIUM → MEDIUM
  * GOOD → HARD
* Theo dõi:

  * accuracy
  * streak
  * performance

**API:**

```bash
POST   /api/practice/start
GET    /api/practice/questions?sessionId=
POST   /api/practice/submit
POST   /api/practice/finish/{sessionId}
```

---

### 📝 Exam (Thi thử)

* Start → làm bài → submit → chấm điểm
* Không trả đáp án khi đang thi
* Score = correct / total × 10

**API:**

```bash
POST   /api/exams/start/{examId}
GET    /api/exams/{resultId}/questions
POST   /api/exams/answer
POST   /api/exams/submit/{resultId}
```

---

### 📊 Performance

* Lưu theo topic:

  * attempts
  * correct
  * accuracy
  * level (WEAK / MEDIUM / GOOD)

---

### 👨‍👩‍👧 Parent

* Liên kết phụ huynh – học sinh
* Theo dõi tiến độ học

---

## 🗄️ CSDL chính

* users, subjects, topics
* questions, answers
* practice_sessions, user_answers
* exams, exam_results, exam_answers
* user_performance

---

## 🔐 Security

* JWT (stateless)
* Không nhận `userId` từ request

```java
SecurityUtils.getCurrentUser()
```

---

## ⚙️ Tech Stack

* Java + Spring Boot
* Spring Security + JWT
* Spring Data JPA
* MySQL

---

## 🌟 Điểm nổi bật

* Adaptive learning (không phải CRUD)
* Full flow thi thật
* Bảo mật chuẩn JWT
* Kiến trúc rõ ràng (Controller → Service → Repository)


