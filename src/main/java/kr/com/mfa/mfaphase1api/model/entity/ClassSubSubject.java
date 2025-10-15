package kr.com.mfa.mfaphase1api.model.entity;

import jakarta.persistence.*;
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
@Table(name = "class_sub_subjects", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"class_id", "sub_subject_id"})
})
public class ClassSubSubject {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID classSubSubjectId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id", nullable = false, foreignKey = @ForeignKey(name = "fk_class_sub_subject"))
    @ToString.Exclude
    private Class clazz;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_subject_id", nullable = false, foreignKey = @ForeignKey(name = "fk_sub_subject_class_sub_subject"))
    @ToString.Exclude
    private SubSubject subSubject;

    @ToString.Exclude
    @OneToMany(mappedBy = "classSubSubject", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    List<ClassSubSubjectInstructor> classSubSubjectInstructors = new ArrayList<>();

}
