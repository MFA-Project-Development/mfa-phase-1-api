package kr.com.mfa.mfaphase1api.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EnrollStudentRequest {

    @NotNull
    private LocalDateTime startDate;

    @NotNull
    @NotBlank
    private String timeZone;


}
