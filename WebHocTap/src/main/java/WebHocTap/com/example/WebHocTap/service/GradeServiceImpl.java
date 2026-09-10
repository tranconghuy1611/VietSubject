package WebHocTap.com.example.WebHocTap.service;

import WebHocTap.com.example.WebHocTap.dto.PageResponse;
import WebHocTap.com.example.WebHocTap.dto.grade.GradeResponse;
import WebHocTap.com.example.WebHocTap.mapper.GradeMapper;
import WebHocTap.com.example.WebHocTap.repository.GradeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GradeServiceImpl implements GradeService {

    private final GradeRepository gradeRepository;
    private final GradeMapper gradeMapper;

    @Override
    public PageResponse<GradeResponse> getAllGrades(Pageable pageable) {
        Pageable sorted = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.ASC, "level"));

        log.debug("Fetching grades page={}, size={}", sorted.getPageNumber(), sorted.getPageSize());
        Page<GradeResponse> page = gradeRepository.findAll(sorted).map(gradeMapper::toResponse);
        return PageResponse.from(page);
    }
}
