package kr.com.mfa.mfaphase1api.model.entity;

import jakarta.persistence.*;
import kr.com.mfa.mfaphase1api.model.dto.response.SubSubjectResponse;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@Entity
@Table(name = "sub_subjects")
public class SubSubject {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID subSubjectId;

    @Column(unique = true, nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false, foreignKey = @ForeignKey(name = "fk_subject_sub_subject"))
    @ToString.Exclude
    private Subject subject;

    @ToString.Exclude
    @OneToMany(mappedBy = "subSubject", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ClassSubSubject> classSubSubjects = new ArrayList<>();

    public SubSubjectResponse toResponse(){
        return SubSubjectResponse.builder()
                .subSubjectId(this.subSubjectId)
                .name(this.name)
                .subjectResponse(this.subject.toResponse())
                .build();
    }

}
