package kr.com.mfa.mfaphase1api.model.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import kr.com.mfa.mfaphase1api.model.dto.response.MotivationCommentResponse;
import kr.com.mfa.mfaphase1api.model.dto.response.MotivationCommentResponseWithReply;
import kr.com.mfa.mfaphase1api.model.dto.response.StudentResponse;
import kr.com.mfa.mfaphase1api.model.dto.response.UserResponse;
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
@Table(name = "motivation_comments")
public class MotivationComment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID motivationCommentId;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", foreignKey = @ForeignKey(name = "fk_comment_parent"))
    @ToString.Exclude
    @JsonBackReference
    private MotivationComment parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @Builder.Default
    @JsonManagedReference
    private List<MotivationComment> replies = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "motivation_content_id", nullable = false, foreignKey = @ForeignKey(name = "fk_motivation_bookmark_motivation_content"))
    @ToString.Exclude
    private MotivationContent motivationContent;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    private String timeZone;

    public MotivationCommentResponse toResponse(UserResponse userResponse) {

        ZoneId zone;
        try {
            zone = this.timeZone != null
                    ? ZoneId.of(this.timeZone)
                    : ZoneId.of("UTC");
        } catch (DateTimeException e) {
            zone = ZoneId.of("UTC");
        }

        return MotivationCommentResponse.builder()
                .motivationCommentId(this.motivationCommentId)
                .comment(this.comment)
                .userResponse(userResponse)
                .motivationContentId(this.motivationContent.getMotivationContentId())
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

    public MotivationCommentResponseWithReply toResponse(UserResponse userResponse, List<MotivationCommentResponseWithReply> replies) {

        ZoneId zone;
        try {
            zone = this.timeZone != null
                    ? ZoneId.of(this.timeZone)
                    : ZoneId.of("UTC");
        } catch (DateTimeException e) {
            zone = ZoneId.of("UTC");
        }

        return MotivationCommentResponseWithReply.builder()
                .motivationCommentId(this.motivationCommentId)
                .comment(this.comment)
                .userResponse(userResponse)
                .motivationContentId(this.motivationContent.getMotivationContentId())
                .replies(replies)
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
