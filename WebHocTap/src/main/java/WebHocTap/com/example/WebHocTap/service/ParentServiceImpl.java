package WebHocTap.com.example.WebHocTap.service;

import WebHocTap.com.example.WebHocTap.dto.user.ChildResponse;
import WebHocTap.com.example.WebHocTap.dto.user.LinkChildRequest;
import WebHocTap.com.example.WebHocTap.entity.ParentChild;
import WebHocTap.com.example.WebHocTap.entity.User;
import WebHocTap.com.example.WebHocTap.enums.Role;
import WebHocTap.com.example.WebHocTap.exception.BadRequestException;
import WebHocTap.com.example.WebHocTap.exception.ResourceNotFoundException;
import WebHocTap.com.example.WebHocTap.mapper.UserMapper;
import WebHocTap.com.example.WebHocTap.repository.ParentChildRepository;
import WebHocTap.com.example.WebHocTap.repository.UserRepository;
import WebHocTap.com.example.WebHocTap.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ParentServiceImpl implements ParentService {

    private final ParentChildRepository parentChildRepository;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public ChildResponse linkChild(LinkChildRequest request) {
        if (request == null || request.getChildId() == null) {
            throw new BadRequestException("childId is required");
        }

        User parent = loadCurrentParent();
        Long childId = request.getChildId();

        if (parent.getId().equals(childId)) {
            throw new BadRequestException("Cannot link yourself as a child");
        }

        User child = userRepository.findById(childId)
                .orElseThrow(() -> new ResourceNotFoundException("Child user not found with id: " + childId));

        if (child.getRole() != Role.STUDENT) {
            throw new BadRequestException("Linked child must have STUDENT role");
        }
        if (Boolean.FALSE.equals(child.getIsActive())) {
            throw new BadRequestException("Cannot link an inactive student");
        }
        if (parentChildRepository.existsByParent_IdAndChild_Id(parent.getId(), childId)) {
            throw new BadRequestException("Child is already linked to this parent");
        }

        ParentChild link = new ParentChild();
        link.setParent(parent);
        link.setChild(child);

        try {
            parentChildRepository.save(link);
        } catch (DataIntegrityViolationException ex) {
            throw new BadRequestException("Child is already linked to this parent");
        }

        log.info("Parent id={} linked child id={}", parent.getId(), childId);
        return userMapper.toChildResponse(child);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChildResponse> getChildren() {
        User parent = loadCurrentParent();
        return parentChildRepository.findByParentIdWithChild(parent.getId()).stream()
                .map(ParentChild::getChild)
                .map(userMapper::toChildResponse)
                .toList();
    }

    @Override
    @Transactional
    public void unlinkChild(Long childId) {
        if (childId == null) {
            throw new BadRequestException("childId is required");
        }

        User parent = loadCurrentParent();

        ParentChild link = parentChildRepository.findByParent_IdAndChild_Id(parent.getId(), childId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Parent-child relationship not found for childId: " + childId));

        parentChildRepository.delete(link);
        log.info("Parent id={} unlinked child id={}", parent.getId(), childId);
    }

    private User loadCurrentParent() {
        User current = SecurityUtils.getCurrentUser();
        User parent = userRepository.findById(current.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + current.getId()));

        if (parent.getRole() != Role.PARENT) {
            throw new BadRequestException("Only PARENT users can manage children");
        }
        if (Boolean.FALSE.equals(parent.getIsActive())) {
            throw new BadRequestException("Inactive parent cannot manage children");
        }
        return parent;
    }
}
