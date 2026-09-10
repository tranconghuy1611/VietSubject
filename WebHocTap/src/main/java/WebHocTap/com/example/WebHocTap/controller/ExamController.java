package WebHocTap.com.example.WebHocTap.controller;

import WebHocTap.com.example.WebHocTap.dto.ApiResponse;
import WebHocTap.com.example.WebHocTap.dto.PageResponse;
import WebHocTap.com.example.WebHocTap.dto.exam.*;
import WebHocTap.com.example.WebHocTap.service.ExamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Exam APIs. userId is NEVER accepted from the client — resolved from JWT.
 * Result endpoints are under {@code /results/**} to avoid conflicting with exam {@code /{id}}.
 */
@RestController
@RequestMapping("/api/exams")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class ExamController {

    private final ExamService examService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ExamResponseDTO>>> listExams(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.<PageResponse<ExamResponseDTO>>builder()
                .status(HttpStatus.OK.value())
                .message("OK")
                .data(examService.listExams(pageable))
                .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ExamResponseDTO>> getExam(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.<ExamResponseDTO>builder()
                .status(HttpStatus.OK.value())
                .message("OK")
                .data(examService.getExamById(id))
                .build());
    }

    @PostMapping("/start/{examId}")
    public ResponseEntity<ApiResponse<ExamStartResponseDTO>> startExam(@PathVariable Long examId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<ExamStartResponseDTO>builder()
                        .status(HttpStatus.CREATED.value())
                        .message("Exam started successfully.")
                        .data(examService.startExam(examId))
                        .build());
    }

    @GetMapping("/results/me")
    public ResponseEntity<ApiResponse<PageResponse<ExamResultDTO>>> getMyResults(
            @PageableDefault(size = 20, sort = "startedAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.<PageResponse<ExamResultDTO>>builder()
                .status(HttpStatus.OK.value())
                .message("OK")
                .data(examService.getMyResults(pageable))
                .build());
    }

    @GetMapping("/results/{resultId}")
    public ResponseEntity<ApiResponse<ExamResultDTO>> getResultDetail(@PathVariable Long resultId) {
        return ResponseEntity.ok(ApiResponse.<ExamResultDTO>builder()
                .status(HttpStatus.OK.value())
                .message("OK")
                .data(examService.getResultDetail(resultId))
                .build());
    }

    @GetMapping("/results/{resultId}/summary")
    public ResponseEntity<ApiResponse<ExamSummaryDTO>> getResultSummary(@PathVariable Long resultId) {
        return ResponseEntity.ok(ApiResponse.<ExamSummaryDTO>builder()
                .status(HttpStatus.OK.value())
                .message("OK")
                .data(examService.getResultSummary(resultId))
                .build());
    }

    @GetMapping("/{resultId}/questions")
    public ResponseEntity<ApiResponse<List<ExamQuestionDTO>>> getQuestions(@PathVariable Long resultId) {
        return ResponseEntity.ok(ApiResponse.<List<ExamQuestionDTO>>builder()
                .status(HttpStatus.OK.value())
                .message("Questions retrieved successfully.")
                .data(examService.getQuestions(resultId))
                .build());
    }

    @PostMapping("/{resultId}/answers")
    public ResponseEntity<ApiResponse<SubmitExamAnswerResponseDTO>> submitAnswer(
            @PathVariable Long resultId,
            @Valid @RequestBody SubmitExamAnswerRequestDTO request) {

        return ResponseEntity.ok(ApiResponse.<SubmitExamAnswerResponseDTO>builder()
                .status(HttpStatus.OK.value())
                .message("Answer saved successfully.")
                .data(examService.submitAnswer(resultId, request))
                .build());
    }

    @PostMapping("/submit/{resultId}")
    public ResponseEntity<ApiResponse<ExamResultDTO>> submitExam(@PathVariable Long resultId) {
        return ResponseEntity.ok(ApiResponse.<ExamResultDTO>builder()
                .status(HttpStatus.OK.value())
                .message("Exam submitted successfully.")
                .data(examService.submitExam(resultId))
                .build());
    }
}
