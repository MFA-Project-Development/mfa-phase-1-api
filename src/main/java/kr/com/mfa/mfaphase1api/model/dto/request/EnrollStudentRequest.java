package kr.com.mfa.mfaphase1api.model.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EnrollStudentRequest {

    @NotNull
    private LocalDate startDate;

}
