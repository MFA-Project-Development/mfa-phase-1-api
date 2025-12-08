package kr.com.mfa.mfaphase1api.model.entity;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import kr.com.mfa.mfaphase1api.model.dto.response.MotivationContentResponse;
import kr.com.mfa.mfaphase1api.model.enums.MotivationContentType;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "motivation_contents")
public class MotivationContent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID motivationContentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MotivationContentType type;

    @Column(nullable = false)
    private Boolean isDefault;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Object contentJson;

    @Column(nullable = false)
    private UUID createdBy;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "motivationContent", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<MotivationSession> motivationSessions = new ArrayList<>();

    public MotivationContentResponse toResponse(){
        return MotivationContentResponse.builder()
                .motivationContentId(this.motivationContentId)
                .type(this.type)
                .isDefault(this.isDefault)
                .contentJson(this.contentJson)
                .createdBy(this.createdBy)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }

}
