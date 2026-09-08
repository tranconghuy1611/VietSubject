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

/**
 * REST controller for the full exam flow.
 *
 * <p><strong>Security note:</strong> userId is NEVER read from any request parameter or body.
 * <ul>
 *   <li>{@code startExam}    — userId extracted from JWT via {@code SecurityUtils}</li>
 *   <li>{@code getQuestions} — userId read from the persisted exam_result row</li>
 *   <li>{@code submitAnswer} — userId read from the persisted exam_result row</li>
 *   <li>{@code submitExam}   — userId read from the persisted exam_result row</li>
 * </ul>
 *
 * <p>All endpoints require an authenticated JWT ({@code @PreAuthorize("isAuthenticated()")}).
 */
@RestController
@RequestMapping("/api/exams")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class ExamController {

    private final ExamService examService;

    // ──────────────────────────────────────────────────────────────────────────
    // POST /api/exams/start/{examId}
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Start an exam.
     *
     * <p>Validates that the exam exists and is active, creates an {@code exam_result}
     * row with {@code status = IN_PROGRESS}, and returns the {@code resultId} the
     * client must use for all subsequent calls.
     *
     * <p>userId is extracted from the JWT — never from the URL or body.
     */
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

    /**
     * Fetch all questions for an active exam result, ordered by {@code question_order}.
     *
     * <p>Answers are returned without the {@code isCorrect} flag.
     * userId is resolved from the persisted {@code exam_result} row — not from the request.
     * Ownership is verified against the JWT user before returning data.
     */
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

    /**
     * Save (or overwrite) a single answer for an active exam result.
     *
     * <p>Request body:
     * <pre>{@code
     * {
     *   "resultId":      1,
     *   "questionId":    42,
     *   "answerId":      7,         // MULTIPLE_CHOICE — provide answerId
     *   "submittedText": null       // FILL_BLANK / LISTENING — provide submittedText
     * }
     * }</pre>
     *
     * <p>userId is derived from the exam_result row. No {@code userId} field is accepted.
     * Calling this endpoint again for the same question overwrites the previous answer.
     */
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
