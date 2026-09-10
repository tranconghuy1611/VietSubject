package WebHocTap.com.example.WebHocTap.service;

import WebHocTap.com.example.WebHocTap.dto.PageResponse;
import WebHocTap.com.example.WebHocTap.dto.question.*;
import WebHocTap.com.example.WebHocTap.enums.Difficulty;
import WebHocTap.com.example.WebHocTap.enums.QuestionType;
import org.springframework.data.domain.Pageable;

public interface QuestionService {

    PageResponse<QuestionResponse> getQuestions(
            Long topicId, QuestionType type, Difficulty difficulty, Pageable pageable);

    QuestionResponse getQuestionById(Long id);

    QuestionResponse createQuestion(CreateQuestionRequest request);

    QuestionResponse updateQuestion(Long id, UpdateQuestionRequest request);

    void softDeleteQuestion(Long id);
}
