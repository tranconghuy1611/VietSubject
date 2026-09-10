package WebHocTap.com.example.WebHocTap.service;

import WebHocTap.com.example.WebHocTap.dto.question.AnswerRequest;
import WebHocTap.com.example.WebHocTap.dto.question.AnswerResponse;
import WebHocTap.com.example.WebHocTap.entity.Answer;
import WebHocTap.com.example.WebHocTap.entity.Question;
import WebHocTap.com.example.WebHocTap.enums.QuestionType;
import WebHocTap.com.example.WebHocTap.exception.BadRequestException;
import WebHocTap.com.example.WebHocTap.exception.ResourceNotFoundException;
import WebHocTap.com.example.WebHocTap.mapper.QuestionMapper;
import WebHocTap.com.example.WebHocTap.repository.AnswerRepository;
import WebHocTap.com.example.WebHocTap.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnswerServiceImpl implements AnswerService {

    private final AnswerRepository answerRepository;
    private final QuestionRepository questionRepository;
    private final QuestionMapper questionMapper;

    @Override
    @Transactional
    public AnswerResponse addAnswer(Long questionId, AnswerRequest request) {
        Question question = questionRepository.findActiveByIdWithAnswers(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with id: " + questionId));
        ensureChoiceType(question);

        if (question.getQuestionType() == QuestionType.MULTIPLE_CHOICE
                && Boolean.TRUE.equals(request.getIsCorrect())
                && question.getAnswers().stream().anyMatch(a -> Boolean.TRUE.equals(a.getIsCorrect()))) {
            throw new BadRequestException("MULTIPLE_CHOICE already has a correct answer");
        }

        Answer answer = new Answer();
        answer.setQuestion(question);
        answer.setContent(request.getContent());
        answer.setImageUrl(request.getImageUrl());
        answer.setIsCorrect(Boolean.TRUE.equals(request.getIsCorrect()));
        question.getAnswers().add(answer);

        Answer saved = answerRepository.save(answer);
        log.info("Added answer id={} to question id={}", saved.getId(), questionId);
        return questionMapper.toAnswerResponse(saved);
    }

    @Override
    @Transactional
    public AnswerResponse updateAnswer(Long answerId, AnswerRequest request) {
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new ResourceNotFoundException("Answer not found with id: " + answerId));

        Question question = questionRepository.findActiveByIdWithAnswers(answer.getQuestion().getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Question not found with id: " + answer.getQuestion().getId()));
        ensureChoiceType(question);

        if (question.getQuestionType() == QuestionType.MULTIPLE_CHOICE
                && Boolean.TRUE.equals(request.getIsCorrect())) {
            boolean otherCorrect = question.getAnswers().stream()
                    .filter(a -> !a.getId().equals(answerId))
                    .anyMatch(a -> Boolean.TRUE.equals(a.getIsCorrect()));
            if (otherCorrect) {
                throw new BadRequestException("MULTIPLE_CHOICE can have only 1 correct answer");
            }
        }

        answer.setContent(request.getContent());
        answer.setImageUrl(request.getImageUrl());
        if (request.getIsCorrect() != null) {
            answer.setIsCorrect(request.getIsCorrect());
        }

        Answer saved = answerRepository.save(answer);
        log.info("Updated answer id={}", saved.getId());
        return questionMapper.toAnswerResponse(saved);
    }

    @Override
    @Transactional
    public void deleteAnswer(Long answerId) {
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new ResourceNotFoundException("Answer not found with id: " + answerId));

        Question question = questionRepository.findActiveByIdWithAnswers(answer.getQuestion().getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Question not found with id: " + answer.getQuestion().getId()));
        ensureChoiceType(question);

        boolean wasCorrect = Boolean.TRUE.equals(answer.getIsCorrect());
        long remainingCorrect = question.getAnswers().stream()
                .filter(a -> !a.getId().equals(answerId))
                .filter(a -> Boolean.TRUE.equals(a.getIsCorrect()))
                .count();
        if (wasCorrect && remainingCorrect == 0) {
            throw new BadRequestException("Cannot delete the only correct answer");
        }
        if (question.getAnswers().size() <= 2) {
            throw new BadRequestException("Choice questions require at least 2 answers");
        }

        question.getAnswers().removeIf(a -> a.getId().equals(answerId));
        questionRepository.save(question);
        log.info("Deleted answer id={} from question id={}", answerId, question.getId());
    }

    private void ensureChoiceType(Question question) {
        QuestionType type = question.getQuestionType();
        if (type != QuestionType.MULTIPLE_CHOICE && type != QuestionType.MULTIPLE_SELECT) {
            throw new BadRequestException("Answers can only be managed for MULTIPLE_CHOICE / MULTIPLE_SELECT");
        }
    }
}
