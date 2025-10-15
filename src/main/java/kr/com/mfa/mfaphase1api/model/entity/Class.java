package kr.com.mfa.mfaphase1api.model.entity;

import jakarta.persistence.*;
import kr.com.mfa.mfaphase1api.model.dto.response.ClassResponse;
import lombok.*;

import java.util.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "classes")
public class Class {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID classId;

    @Column(unique = true, nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String code;

    @ToString.Exclude
    @OneToMany(mappedBy = "clazz", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ClassSubSubject> classSubSubjects = new ArrayList<>();

    @ToString.Exclude
    @OneToMany(mappedBy = "clazz", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<StudentClassEnrollment> studentClassEnrollments = new ArrayList<>();

    public ClassResponse toResponse() {
        return ClassResponse.builder()
                .classId(this.classId)
                .name(this.name)
                .code(this.code)
                .build();
    }

}
