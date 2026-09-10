package WebHocTap.com.example.WebHocTap.controller;

import WebHocTap.com.example.WebHocTap.dto.ApiResponse;
import WebHocTap.com.example.WebHocTap.dto.exam.*;
import WebHocTap.com.example.WebHocTap.service.ExamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exams")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class ExamController {

    private final ExamService examService;

    // ──────────────────────────────────────────────────────────────────────────
    // POST /api/exams/start/{examId}
    // ──────────────────────────────────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<ApiResponse<List<ExamListDTO>>> getAllExams() {
        List<ExamListDTO> data = examService.getAllExams();
        return ResponseEntity.ok(ApiResponse.<List<ExamListDTO>>builder()
                .status(HttpStatus.OK.value())
                .message("Exams retrieved successfully.")
                .data(data)
                .build());
    }
    @GetMapping("/results/me")
    public ResponseEntity<ApiResponse<List<ExamResultDTO>>> getMyResults() {

        List<ExamResultDTO> data = examService.getMyResults();

        return ResponseEntity.ok(ApiResponse.<List<ExamResultDTO>>builder()
                .status(HttpStatus.OK.value())
                .message("Exam history retrieved successfully.")
                .data(data)
                .build());
    }
    @GetMapping("/{resultId}/review")
    public ResponseEntity<ApiResponse<List<ExamReviewDTO>>> reviewExam(
            @PathVariable Long resultId) {

        List<ExamReviewDTO> data = examService.reviewExam(resultId);

        return ResponseEntity.ok(ApiResponse.<List<ExamReviewDTO>>builder()
                .status(HttpStatus.OK.value())
                .message("Review retrieved successfully.")
                .data(data)
                .build());
    }

    @PostMapping("/{resultId}/answers")
    public ResponseEntity<ApiResponse<SubmitExamAnswerResponseDTO>> submitAnswerV2(
            @PathVariable Long resultId,
            @RequestBody SubmitExamAnswerRequestDTO request) {

        request.setResultId(resultId); // reuse DTO cũ

        SubmitExamAnswerResponseDTO result =
                examService.submitAnswer(request);

        return ResponseEntity.ok(ApiResponse.<SubmitExamAnswerResponseDTO>builder()
                .status(HttpStatus.OK.value())
                .message("Answer saved successfully.")
                .data(result)
                .build());
    }
    @PostMapping("/start/{examId}")
    public ResponseEntity<ApiResponse<ExamStartResponseDTO>> startExam(
            @PathVariable Long examId) {

        ExamStartResponseDTO data = examService.startExam(examId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<ExamStartResponseDTO>builder()
                        .status(HttpStatus.CREATED.value())
                        .message("Exam started successfully.")
                        .data(data)
                        .build());
    }

    // ──────────────────────────────────────────────────────────────────────────
    // GET /api/exams/{resultId}/questions
    // ──────────────────────────────────────────────────────────────────────────

    @GetMapping("/{resultId}/questions")
    public ResponseEntity<ApiResponse<List<ExamQuestionDTO>>> getQuestions(
            @PathVariable Long resultId) {

        List<ExamQuestionDTO> questions = examService.getQuestions(resultId);
        return ResponseEntity.ok(ApiResponse.<List<ExamQuestionDTO>>builder()
                .status(HttpStatus.OK.value())
                .message("Questions retrieved successfully.")
                .data(questions)
                .build());
    }

    // ──────────────────────────────────────────────────────────────────────────
    // POST /api/exams/answer
    // ──────────────────────────────────────────────────────────────────────────


    @PostMapping("/answer")
    public ResponseEntity<ApiResponse<SubmitExamAnswerResponseDTO>> submitAnswer(
            @RequestBody SubmitExamAnswerRequestDTO request) {

        SubmitExamAnswerResponseDTO result = examService.submitAnswer(request);
        return ResponseEntity.ok(ApiResponse.<SubmitExamAnswerResponseDTO>builder()
                .status(HttpStatus.OK.value())
                .message("Answer saved successfully.")
                .data(result)
                .build());
    }

    // ──────────────────────────────────────────────────────────────────────────
    // POST /api/exams/submit/{resultId}
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Grade the exam, persist the final score, and mark the result as SUBMITTED.
     *
     * <p>Business rules enforced by the service layer:
     * <ul>
     *   <li>Cannot submit twice (throws {@code 400} if already SUBMITTED)</li>
     *   <li>Ownership verified — JWT user must match result owner</li>
     *   <li>Score = (correct_count / total_questions) × 10, rounded to two decimals</li>
     *   <li>UserPerformance updated per topic based on the submitted answers</li>
     * </ul>
     *
     * <p>userId comes from the exam_result row — NOT from the URL or request body.
     */
    @PostMapping("/submit/{resultId}")
    public ResponseEntity<ApiResponse<ExamResultDTO>> submitExam(
            @PathVariable Long resultId) {

        ExamResultDTO result = examService.submitExam(resultId);
        return ResponseEntity.ok(ApiResponse.<ExamResultDTO>builder()
                .status(HttpStatus.OK.value())
                .message("Exam submitted successfully.")
                .data(result)
                .build());
    }
}
