package kr.com.mfa.mfaphase1api.model.entity;

import jakarta.persistence.*;
import kr.com.mfa.mfaphase1api.model.dto.response.ModuleTypeResponse;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "module_types")
public class ModuleType {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID moduleTypeId;

    @Column(nullable = false, unique = true)
    private String type;

    public ModuleTypeResponse toResponse() {
        return ModuleTypeResponse.builder()
                .moduleTypeId(this.moduleTypeId)
                .type(this.type)
                .build();
    }

}
