package kr.com.mfa.mfaphase1api.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AssignInstructorRequest {

    @NotNull
    private LocalDateTime startDate;

    @NotNull
    @NotBlank
    private String timeZone;

}
