package WebHocTap.com.example.WebHocTap.controller;

import WebHocTap.com.example.WebHocTap.dto.ApiResponse;
import WebHocTap.com.example.WebHocTap.dto.PageResponse;
import WebHocTap.com.example.WebHocTap.dto.question.*;
import WebHocTap.com.example.WebHocTap.enums.Difficulty;
import WebHocTap.com.example.WebHocTap.enums.QuestionType;
import WebHocTap.com.example.WebHocTap.service.AnswerService;
import WebHocTap.com.example.WebHocTap.service.QuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;
    private final AnswerService answerService;

    @GetMapping
    @PreAuthorize("hasAnyRole('STUDENT', 'PARENT', 'ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<QuestionResponse>>> getQuestions(
            @RequestParam(required = false) Long topicId,
            @RequestParam(required = false) QuestionType type,
            @RequestParam(required = false) Difficulty difficulty,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.<PageResponse<QuestionResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("OK")
                .data(questionService.getQuestions(topicId, type, difficulty, pageable))
                .build());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('STUDENT', 'PARENT', 'ADMIN')")
    public ResponseEntity<ApiResponse<QuestionResponse>> getQuestionById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.<QuestionResponse>builder()
                .status(HttpStatus.OK.value())
                .message("OK")
                .data(questionService.getQuestionById(id))
                .build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<QuestionResponse>> createQuestion(
            @Valid @RequestBody CreateQuestionRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<QuestionResponse>builder()
                        .status(HttpStatus.CREATED.value())
                        .message("Question created successfully")
                        .data(questionService.createQuestion(request))
                        .build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<QuestionResponse>> updateQuestion(
            @PathVariable Long id,
            @Valid @RequestBody UpdateQuestionRequest request) {

        return ResponseEntity.ok(ApiResponse.<QuestionResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Question updated successfully")
                .data(questionService.updateQuestion(id, request))
                .build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteQuestion(@PathVariable Long id) {
        questionService.softDeleteQuestion(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status(HttpStatus.OK.value())
                .message("Question deleted successfully")
                .build());
    }

    @PostMapping("/{id}/answers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AnswerResponse>> addAnswer(
            @PathVariable Long id,
            @Valid @RequestBody AnswerRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<AnswerResponse>builder()
                        .status(HttpStatus.CREATED.value())
                        .message("Answer added successfully")
                        .data(answerService.addAnswer(id, request))
                        .build());
    }
}
