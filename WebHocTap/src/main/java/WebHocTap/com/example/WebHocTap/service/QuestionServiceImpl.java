package WebHocTap.com.example.WebHocTap.service;

import WebHocTap.com.example.WebHocTap.dto.PageResponse;
import WebHocTap.com.example.WebHocTap.dto.question.*;
import WebHocTap.com.example.WebHocTap.entity.Answer;
import WebHocTap.com.example.WebHocTap.entity.Question;
import WebHocTap.com.example.WebHocTap.entity.Topic;
import WebHocTap.com.example.WebHocTap.enums.Difficulty;
import WebHocTap.com.example.WebHocTap.enums.QuestionType;
import WebHocTap.com.example.WebHocTap.exception.BadRequestException;
import WebHocTap.com.example.WebHocTap.exception.ResourceNotFoundException;
import WebHocTap.com.example.WebHocTap.mapper.QuestionMapper;
import WebHocTap.com.example.WebHocTap.repository.QuestionRepository;
import WebHocTap.com.example.WebHocTap.repository.TopicRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {

    private final QuestionRepository questionRepository;
    private final TopicRepository topicRepository;
    private final QuestionMapper questionMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<QuestionResponse> getQuestions(
            Long topicId, QuestionType type, Difficulty difficulty, Pageable pageable) {

        if (topicId != null) {
            topicRepository.findById(topicId)
                    .orElseThrow(() -> new ResourceNotFoundException("Topic not found with id: " + topicId));
        }

        Page<QuestionResponse> page = questionRepository
                .findActiveFiltered(topicId, type, difficulty, pageable)
                .map(questionMapper::toListItem);
        return PageResponse.from(page);
    }

    @Override
    @Transactional(readOnly = true)
    public QuestionResponse getQuestionById(Long id) {
        Question question = questionRepository.findActiveByIdWithAnswers(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with id: " + id));
        return questionMapper.toDetail(question);
    }

    @Override
    @Transactional
    public QuestionResponse createQuestion(CreateQuestionRequest request) {
        Topic topic = topicRepository.findById(request.getTopicId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Topic not found with id: " + request.getTopicId()));

        validateTypePayload(request.getQuestionType(), request.getAnswers(), request.getCorrectTextAnswer());

        Question question = new Question();
        question.setTopic(topic);
        question.setContent(request.getContent().trim());
        question.setQuestionType(request.getQuestionType());
        question.setDifficulty(request.getDifficulty() != null ? request.getDifficulty() : Difficulty.MEDIUM);
        question.setImageUrl(request.getImageUrl());
        question.setAudioUrl(request.getAudioUrl());
        question.setExplanation(request.getExplanation());
        question.setIsActive(true);

        if (request.getQuestionType() == QuestionType.FILL_BLANK
                || request.getQuestionType() == QuestionType.LISTENING) {
            question.setCorrectTextAnswer(request.getCorrectTextAnswer());
            question.setAcceptedTextAnswers(request.getAcceptedTextAnswers());
        }

        replaceAnswers(question, request.getQuestionType(), request.getAnswers());

        Question saved = questionRepository.save(question);
        log.info("Created question id={} type={}", saved.getId(), saved.getQuestionType());
        return questionMapper.toDetail(saved);
    }

    @Override
    @Transactional
    public QuestionResponse updateQuestion(Long id, UpdateQuestionRequest request) {
        Question question = questionRepository.findActiveByIdWithAnswers(id)
                .orElseGet(() -> questionRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Question not found with id: " + id)));

        if (request.getTopicId() != null) {
            Topic topic = topicRepository.findById(request.getTopicId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Topic not found with id: " + request.getTopicId()));
            question.setTopic(topic);
        }
        if (StringUtils.hasText(request.getContent())) {
            question.setContent(request.getContent().trim());
        }
        if (request.getQuestionType() != null) {
            question.setQuestionType(request.getQuestionType());
        }
        if (request.getDifficulty() != null) {
            question.setDifficulty(request.getDifficulty());
        }
        if (request.getImageUrl() != null) {
            question.setImageUrl(request.getImageUrl());
        }
        if (request.getAudioUrl() != null) {
            question.setAudioUrl(request.getAudioUrl());
        }
        if (request.getExplanation() != null) {
            question.setExplanation(request.getExplanation());
        }
        if (request.getIsActive() != null) {
            question.setIsActive(request.getIsActive());
        }
        if (request.getCorrectTextAnswer() != null) {
            question.setCorrectTextAnswer(request.getCorrectTextAnswer());
        }
        if (request.getAcceptedTextAnswers() != null) {
            question.setAcceptedTextAnswers(request.getAcceptedTextAnswers());
        }

        QuestionType type = question.getQuestionType();
        if (request.getAnswers() != null) {
            validateTypePayload(type, request.getAnswers(),
                    request.getCorrectTextAnswer() != null
                            ? request.getCorrectTextAnswer()
                            : question.getCorrectTextAnswer());
            replaceAnswers(question, type, request.getAnswers());
        }

        Question saved = questionRepository.save(question);
        log.info("Updated question id={}", saved.getId());
        return questionMapper.toDetail(saved);
    }

    @Override
    @Transactional
    public void softDeleteQuestion(Long id) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with id: " + id));
        question.setIsActive(false);
        questionRepository.save(question);
        log.info("Soft-deleted question id={}", id);
    }

    private void validateTypePayload(QuestionType type, List<AnswerRequest> answers, String correctTextAnswer) {
        switch (type) {
            case MULTIPLE_CHOICE -> {
                requireAnswers(answers);
                long correct = answers.stream().filter(a -> Boolean.TRUE.equals(a.getIsCorrect())).count();
                if (correct != 1) {
                    throw new BadRequestException("MULTIPLE_CHOICE requires exactly 1 correct answer");
                }
            }
            case MULTIPLE_SELECT -> {
                requireAnswers(answers);
                long correct = answers.stream().filter(a -> Boolean.TRUE.equals(a.getIsCorrect())).count();
                if (correct < 1) {
                    throw new BadRequestException("MULTIPLE_SELECT requires at least 1 correct answer");
                }
            }
            case FILL_BLANK, LISTENING -> {
                if (!StringUtils.hasText(correctTextAnswer)) {
                    throw new BadRequestException(type + " requires correctTextAnswer");
                }
            }
            default -> { }
        }
    }

    private void requireAnswers(List<AnswerRequest> answers) {
        if (answers == null || answers.size() < 2) {
            throw new BadRequestException("Choice questions require at least 2 answers");
        }
    }

    private void replaceAnswers(Question question, QuestionType type, List<AnswerRequest> answers) {
        question.getAnswers().clear();
        if (type != QuestionType.MULTIPLE_CHOICE && type != QuestionType.MULTIPLE_SELECT) {
            return;
        }
        if (answers == null) {
            return;
        }
        for (AnswerRequest req : answers) {
            Answer answer = new Answer();
            answer.setQuestion(question);
            answer.setContent(req.getContent());
            answer.setImageUrl(req.getImageUrl());
            answer.setIsCorrect(Boolean.TRUE.equals(req.getIsCorrect()));
            question.getAnswers().add(answer);
        }
    }
}
