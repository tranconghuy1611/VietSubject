package WebHocTap.com.example.WebHocTap.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "parent_child",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_parent_child",
                columnNames = {"parent_id", "child_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ParentChild {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "parent_id", nullable = false)
    @JsonIgnore
    @ToString.Exclude
    private User parent;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "child_id", nullable = false)
    @JsonIgnore
    @ToString.Exclude
    private User child;
}
