package kr.com.mfa.mfaphase1api.model.entity;

import jakarta.persistence.*;
import kr.com.mfa.mfaphase1api.model.enums.ResourceKind;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "resources")
public class Resource {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID resourceId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ResourceKind kind;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assessment_id", nullable = false, foreignKey = @ForeignKey(name = "fk_resource_assessment"))
    @ToString.Exclude
    private Assessment assessment;

}
