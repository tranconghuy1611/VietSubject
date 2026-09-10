package WebHocTap.com.example.WebHocTap.repository;

import WebHocTap.com.example.WebHocTap.entity.User;
import WebHocTap.com.example.WebHocTap.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    @Query("""
            SELECT u FROM User u
            WHERE (:role IS NULL OR u.role = :role)
              AND (:isActive IS NULL OR u.isActive = :isActive)
            """)
    Page<User> findAllFiltered(
            @Param("role") Role role,
            @Param("isActive") Boolean isActive,
            Pageable pageable);
}
