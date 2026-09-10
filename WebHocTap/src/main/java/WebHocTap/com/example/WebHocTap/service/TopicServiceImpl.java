package WebHocTap.com.example.WebHocTap.service;

import WebHocTap.com.example.WebHocTap.dto.topic.CreateTopicRequest;
import WebHocTap.com.example.WebHocTap.dto.topic.TopicResponse;
import WebHocTap.com.example.WebHocTap.dto.topic.UpdateTopicRequest;
import WebHocTap.com.example.WebHocTap.entity.Grade;
import WebHocTap.com.example.WebHocTap.entity.Subject;
import WebHocTap.com.example.WebHocTap.entity.Topic;
import WebHocTap.com.example.WebHocTap.enums.Difficulty;
import WebHocTap.com.example.WebHocTap.exception.BadRequestException;
import WebHocTap.com.example.WebHocTap.exception.ResourceNotFoundException;
import WebHocTap.com.example.WebHocTap.mapper.TopicMapper;
import WebHocTap.com.example.WebHocTap.repository.GradeRepository;
import WebHocTap.com.example.WebHocTap.repository.SubjectRepository;
import WebHocTap.com.example.WebHocTap.repository.TopicRepository;
import WebHocTap.com.example.WebHocTap.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class TopicServiceImpl implements TopicService {

    private final TopicRepository topicRepository;
    private final SubjectRepository subjectRepository;
    private final GradeRepository gradeRepository;
    private final TopicMapper topicMapper;

    @Override
    @Transactional(readOnly = true)
    public TopicResponse getTopicById(Long id) {
        Topic topic = topicRepository.findByIdWithSubjectAndGrade(id)
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found with id: " + id));
        return topicMapper.toResponse(topic);
    }

    @Override
    @Transactional
    public TopicResponse createTopic(CreateTopicRequest request) {
        String username = currentUsername();
        log.info("Admin '{}' creating topic name='{}'", username, request.getName());

        validateCreateRequest(request);

        Subject subject = findSubjectOrThrow(request.getSubjectId());
        Grade grade = findGradeOrThrow(request.getGradeId());

        Topic topic = new Topic();
        topic.setName(request.getName().trim());
        topic.setSubject(subject);
        topic.setGrade(grade);
        topic.setDifficulty(request.getDifficulty() != null ? request.getDifficulty() : Difficulty.MEDIUM);
        topic.setIsActive(true);

        Topic saved = topicRepository.save(topic);
        log.info("Topic created id={} by '{}'", saved.getId(), username);
        return topicMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public TopicResponse updateTopic(Long id, UpdateTopicRequest request) {
        String username = currentUsername();
        log.info("Admin '{}' updating topic id={}", username, id);

        if (request == null) {
            throw new BadRequestException("Update request body is required");
        }

        Topic topic = topicRepository.findByIdWithSubjectAndGrade(id)
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found with id: " + id));

        if (StringUtils.hasText(request.getName())) {
            topic.setName(request.getName().trim());
        } else if (request.getName() != null) {
            throw new BadRequestException("Topic name must not be blank");
        }

        if (request.getSubjectId() != null) {
            topic.setSubject(findSubjectOrThrow(request.getSubjectId()));
        }
        if (request.getGradeId() != null) {
            topic.setGrade(findGradeOrThrow(request.getGradeId()));
        }
        if (request.getDifficulty() != null) {
            topic.setDifficulty(request.getDifficulty());
        }
        if (request.getIsActive() != null) {
            topic.setIsActive(request.getIsActive());
        }

        Topic saved = topicRepository.save(topic);
        log.info("Topic updated id={} by '{}'", saved.getId(), username);
        return topicMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteTopic(Long id) {
        String username = currentUsername();
        log.info("Admin '{}' deleting (soft) topic id={}", username, id);

        Topic topic = topicRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found with id: " + id));

        topic.setIsActive(false);
        topicRepository.save(topic);
        log.info("Topic soft-deleted id={} by '{}'", id, username);
    }

    private void validateCreateRequest(CreateTopicRequest request) {
        if (request == null) {
            throw new BadRequestException("Create request body is required");
        }
        if (!StringUtils.hasText(request.getName())) {
            throw new BadRequestException("Topic name is required");
        }
        if (request.getSubjectId() == null) {
            throw new BadRequestException("subjectId is required");
        }
        if (request.getGradeId() == null) {
            throw new BadRequestException("gradeId is required");
        }
    }

    private Subject findSubjectOrThrow(Long subjectId) {
        return subjectRepository.findById(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + subjectId));
    }

    private Grade findGradeOrThrow(Long gradeId) {
        return gradeRepository.findById(gradeId)
                .orElseThrow(() -> new ResourceNotFoundException("Grade not found with id: " + gradeId));
    }

    /**
     * Resolves the current authenticated username from SecurityContext.
     * Never accepts userId from the client request.
     */
    private String currentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !StringUtils.hasText(auth.getName())) {
            // Fallback via project helper when principal is CustomUserDetails
            return SecurityUtils.getCurrentUser().getUsername();
        }
        return auth.getName();
    }
}
