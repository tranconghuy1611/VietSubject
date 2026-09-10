package WebHocTap.com.example.WebHocTap.service;

import WebHocTap.com.example.WebHocTap.dto.question.AnswerRequest;
import WebHocTap.com.example.WebHocTap.dto.question.AnswerResponse;

public interface AnswerService {

    AnswerResponse addAnswer(Long questionId, AnswerRequest request);

    AnswerResponse updateAnswer(Long answerId, AnswerRequest request);

    void deleteAnswer(Long answerId);
}
