package WebHocTap.com.example.WebHocTap.service;

import WebHocTap.com.example.WebHocTap.dto.PageResponse;
import WebHocTap.com.example.WebHocTap.dto.subject.SubjectResponse;
import WebHocTap.com.example.WebHocTap.dto.topic.TopicResponse;
import WebHocTap.com.example.WebHocTap.entity.Subject;
import WebHocTap.com.example.WebHocTap.exception.ResourceNotFoundException;
import WebHocTap.com.example.WebHocTap.mapper.SubjectMapper;
import WebHocTap.com.example.WebHocTap.mapper.TopicMapper;
import WebHocTap.com.example.WebHocTap.repository.GradeRepository;
import WebHocTap.com.example.WebHocTap.repository.SubjectRepository;
import WebHocTap.com.example.WebHocTap.repository.TopicRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubjectServiceImpl implements SubjectService {

    private final SubjectRepository subjectRepository;
    private final TopicRepository topicRepository;
    private final GradeRepository gradeRepository;
    private final SubjectMapper subjectMapper;
    private final TopicMapper topicMapper;

    @Override
    public PageResponse<SubjectResponse> getAllSubjects(Pageable pageable) {
        log.debug("Fetching subjects page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        Page<SubjectResponse> page = subjectRepository.findAllByOrderByNameAsc(pageable)
                .map(subjectMapper::toResponse);
        return PageResponse.from(page);
    }

    @Override
    public PageResponse<TopicResponse> getTopicsBySubject(Long subjectId, Long gradeId, Pageable pageable) {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + subjectId));

        if (gradeId != null) {
            gradeRepository.findById(gradeId)
                    .orElseThrow(() -> new ResourceNotFoundException("Grade not found with id: " + gradeId));
        }

        log.debug("Fetching active topics for subjectId={}, gradeId={}", subject.getId(), gradeId);
        Page<TopicResponse> page = topicRepository
                .findActiveBySubjectIdAndOptionalGradeId(subjectId, gradeId, pageable)
                .map(topicMapper::toResponse);
        return PageResponse.from(page);
    }
}
