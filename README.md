# 📘 Nền tảng Luyện tập & Thi Thông minh cho Học sinh Tiểu học

Hệ thống EdTech được xây dựng nhằm giúp học sinh tiểu học (lớp 1–5) **luyện tập hiệu quả, thi thử và cải thiện kết quả học tập** thông qua cơ chế học thích ứng (adaptive learning).

---

## 🚀 Tổng quan

Đây **không phải** là hệ thống học lý thuyết.

Hệ thống tập trung vào:

* ✍️ Luyện tập thông minh (Practice)
* 📝 Thi thử (Exam)
* 📊 Phân tích kết quả học tập (Performance)
* 👨‍👩‍👧 Theo dõi từ phụ huynh

Backend được xây dựng theo hướng **production-ready** với:

* Spring Boot
* JWT Authentication
* MySQL

---

## 🧠 Chức năng chính

---

### 1. 🔐 Xác thực & Quản lý người dùng

* Đăng nhập bằng JWT (stateless)
* Refresh token
* Đăng nhập Google (OAuth2)
* Phân quyền:

  * `STUDENT`
  * `PARENT`
  * `ADMIN`

---

### 2. 📚 Hệ thống Luyện tập (Adaptive Learning)

#### 🔄 Luồng hoạt động:

1. Bắt đầu session
2. Lấy câu hỏi (theo độ khó phù hợp)
3. Trả lời câu hỏi
4. Kết thúc session

#### 🔥 Điểm nổi bật:

* Độ khó thích ứng:

  * WEAK → EASY
  * MEDIUM → MEDIUM
  * GOOD → HARD
* Tránh lặp câu hỏi gần đây
* Theo dõi:

  * độ chính xác
  * số lần làm
  * streak học tập
* Gợi ý học tập (recommendation)

#### 📌 API:

```bash
POST   /api/practice/start
GET    /api/practice/questions?sessionId=
POST   /api/practice/submit
POST   /api/practice/finish/{sessionId}
```

---

### 3. 📝 Hệ thống Thi (Exam System)

#### 🔄 Luồng thi:

1. Start exam → tạo `exam_result`
2. Lấy danh sách câu hỏi
3. Gửi câu trả lời (có thể sửa)
4. Nộp bài → chấm điểm

#### 📏 Business logic:

* Không được nộp 2 lần
* Điểm = (số câu đúng / tổng câu) × 10
* Kiểm tra quyền bằng JWT
* Không trả về đáp án đúng khi đang làm bài

#### 📌 API:

```bash
POST   /api/exams/start/{examId}
GET    /api/exams/{resultId}/questions
POST   /api/exams/answer
POST   /api/exams/submit/{resultId}
```

---

### 4. 📊 Theo dõi hiệu suất học tập

Lưu theo từng topic:

* total_attempts
* correct_answers
* accuracy
* level:

  * WEAK (<50%)
  * MEDIUM (50–80%)
  * GOOD (>80%)

👉 Dùng cho:

* Adaptive learning
* Gợi ý học tập
* Dashboard phụ huynh

---

### 5. 👨‍👩‍👧 Chức năng Phụ huynh

* Liên kết tài khoản con
* Theo dõi kết quả học
* Xem tiến độ học tập

#### 📌 API:

```bash
POST   /api/parents/link-child
GET    /api/parents/children
DELETE /api/parents/unlink/{childId}
```

---

## 🗄️ Thiết kế CSDL (Tóm tắt)

### 📌 Core:

* users
* subjects
* topics
* questions
* answers

---

### 📚 Practice:

* practice_sessions
* user_answers
* user_performance
* user_streak
* recommendations

---

### 📝 Exam:

* exams
* exam_questions
* exam_results
* exam_answers

---

## 🔐 Bảo mật

* Sử dụng JWT (stateless)
* Không bao giờ nhận `userId` từ request

```java
SecurityUtils.getCurrentUser()
```

* Kiểm tra quyền:

```java
@PreAuthorize("isAuthenticated()")
```

---

## ⚙️ Công nghệ sử dụng

### Backend:

* Java 17 / 21
* Spring Boot
* Spring Security
* Spring Data JPA
* MySQL

### Khác:

* JWT
* OAuth2 (Google)
* Lombok

---

## 🧱 Cấu trúc project

```bash
src/main/java/.../WebHocTap
│
├── controller/
├── service/
├── repository/
├── entity/
├── dto/
├── security/
├── exception/
└── config/
```

---

## 🧪 Logic nổi bật

### ✅ Adaptive Learning

* Dựa vào `user_performance`
* Loại bỏ câu hỏi đã làm gần đây
* Hỗ trợ:

  * theo topic
  * theo subject

---

### ✅ Chấm bài thi

* Trắc nghiệm → check `isCorrect`
* Điền từ → so sánh text (có đáp án thay thế)

---

### ✅ Cập nhật Performance

```text
accuracy = correct / total

<50%   → WEAK
50–80  → MEDIUM
>80    → GOOD
```

---

## 🌟 Điểm mạnh (Đưa vào CV)

* Kiến trúc chuẩn (Controller → Service → Repository)
* Bảo mật JWT chuẩn doanh nghiệp
* Adaptive learning logic (không phải CRUD đơn giản)
* Full flow thi thật (start → làm → submit → chấm điểm)
* Clean code + tách lớp rõ ràng
* Có thể mở rộng (Redis, Kafka, AI)

---

## 🚀 Hướng phát triển

* Redis cache
* Kafka tracking
* WebSocket leaderboard
* AI giải thích bài
* Mobile app

---

## 👨‍💻 Tác giả

* Java Backend Developer (Intern/Fresher)
* Định hướng: xây dựng hệ thống backend thực tế



