package WebHocTap.com.example.WebHocTap.repository;

import WebHocTap.com.example.WebHocTap.entity.ParentChild;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParentChildRepository extends JpaRepository<ParentChild, Long> {

    boolean existsByParent_IdAndChild_Id(Long parentId, Long childId);

    void deleteByParent_IdAndChild_Id(Long parentId, Long childId);

    Optional<ParentChild> findByParent_IdAndChild_Id(Long parentId, Long childId);

    @Query("""
            SELECT pc FROM ParentChild pc
            JOIN FETCH pc.child
            WHERE pc.parent.id = :parentId
            """)
    List<ParentChild> findByParentIdWithChild(@Param("parentId") Long parentId);
}
