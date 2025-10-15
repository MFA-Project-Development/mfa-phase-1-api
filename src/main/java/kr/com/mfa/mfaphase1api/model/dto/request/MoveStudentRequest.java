package kr.com.mfa.mfaphase1api.model.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MoveStudentRequest {

    @NotNull
    private UUID studentId;

    @NotNull
    private UUID fromClassId;

    @NotNull
    private UUID toClassId;

    @NotNull
    private LocalDate effectiveDate;

    private String moveReason;

}
