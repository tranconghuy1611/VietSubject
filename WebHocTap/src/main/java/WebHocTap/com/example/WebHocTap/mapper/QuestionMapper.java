package WebHocTap.com.example.WebHocTap.mapper;

import WebHocTap.com.example.WebHocTap.dto.question.AnswerResponse;
import WebHocTap.com.example.WebHocTap.dto.question.QuestionResponse;
import WebHocTap.com.example.WebHocTap.entity.Answer;
import WebHocTap.com.example.WebHocTap.entity.Question;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class QuestionMapper {

    public QuestionResponse toListItem(Question question) {
        return QuestionResponse.builder()
                .id(question.getId())
                .topicId(question.getTopic() != null ? question.getTopic().getId() : null)
                .content(question.getContent())
                .type(question.getQuestionType())
                .difficulty(question.getDifficulty())
                .imageUrl(question.getImageUrl())
                .audioUrl(question.getAudioUrl())
                .build();
    }

    public QuestionResponse toDetail(Question question) {
        return QuestionResponse.builder()
                .id(question.getId())
                .topicId(question.getTopic() != null ? question.getTopic().getId() : null)
                .content(question.getContent())
                .type(question.getQuestionType())
                .difficulty(question.getDifficulty())
                .imageUrl(question.getImageUrl())
                .audioUrl(question.getAudioUrl())
                .answers(toAnswerResponses(question.getAnswers()))
                .build();
    }

    public AnswerResponse toAnswerResponse(Answer answer) {
        return AnswerResponse.builder()
                .id(answer.getId())
                .content(answer.getContent())
                .imageUrl(answer.getImageUrl())
                .build();
    }

    public List<AnswerResponse> toAnswerResponses(List<Answer> answers) {
        if (answers == null || answers.isEmpty()) {
            return Collections.emptyList();
        }
        return answers.stream().map(this::toAnswerResponse).collect(Collectors.toList());
    }
}
