package kr.com.mfa.mfaphase1api.model.entity;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import kr.com.mfa.mfaphase1api.model.dto.response.MotivationContentResponse;
import kr.com.mfa.mfaphase1api.model.enums.MotivationContentType;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    private String timeZone;


    @OneToMany(mappedBy = "motivationContent", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<MotivationSession> motivationSessions = new ArrayList<>();

    public MotivationContentResponse toResponse() {

        ZoneId zone;
        try {
            zone = this.timeZone != null
                    ? ZoneId.of(this.timeZone)
                    : ZoneId.of("UTC");
        } catch (DateTimeException e) {
            zone = ZoneId.of("UTC");
        }

        return MotivationContentResponse.builder()
                .motivationContentId(this.motivationContentId)
                .type(this.type)
                .isDefault(this.isDefault)
                .contentJson(this.contentJson)
                .createdBy(this.createdBy)
                .createdAt(
                        this.createdAt != null
                                ? LocalDateTime.ofInstant(this.createdAt, zone)
                                : null
                )
                .updatedAt(
                        this.updatedAt != null
                                ? LocalDateTime.ofInstant(this.updatedAt, zone)
                                : null
                )
                .build();
    }

}
