package kr.com.mfa.mfaphase1api.model.entity;

import jakarta.persistence.*;
import kr.com.mfa.mfaphase1api.model.dto.response.SubjectResponse;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
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
@Table(name = "subjects")
public class Subject {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID subjectId;

    @Column(nullable = false, unique = true)
    private String name;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    @Column(nullable = false)
    private String timeZone;

    @OneToMany(mappedBy = "subject", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<SubSubject> subjects = new ArrayList<>();

    public SubjectResponse toResponse() {

        ZoneId zone;
        try {
            zone = this.timeZone != null
                    ? ZoneId.of(this.timeZone)
                    : ZoneId.of("UTC");
        } catch (DateTimeException e) {
            zone = ZoneId.of("UTC");
        }

        return SubjectResponse.builder()
                .subjectId(this.subjectId)
                .name(this.name)
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
