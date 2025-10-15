package kr.com.mfa.mfaphase1api.model.entity;

import jakarta.persistence.*;
import kr.com.mfa.mfaphase1api.model.dto.response.SubjectResponse;
import lombok.*;

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

    public SubjectResponse toResponse(){
        return SubjectResponse.builder()
                .subjectId(this.subjectId)
                .name(this.name)
                .build();
    }

}
