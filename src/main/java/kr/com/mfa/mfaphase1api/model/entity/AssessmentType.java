package kr.com.mfa.mfaphase1api.model.entity;

import jakarta.persistence.*;
import kr.com.mfa.mfaphase1api.model.dto.response.AssessmentTypeResponse;
import lombok.*;

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
@Table(name = "assessment_types")
public class AssessmentType {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID assessmentTypeId;

    @Column(nullable = false, unique = true)
    private String type;

//    @ToString.Exclude
//    @OneToMany(mappedBy = "assessmentType", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
//    private List<Assessment> assessments = new ArrayList<>();

    public AssessmentTypeResponse toResponse() {
        return AssessmentTypeResponse.builder()
                .assessmentTypeId(this.assessmentTypeId)
                .type(this.type)
                .build();
    }
}
