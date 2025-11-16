package kr.com.mfa.mfaphase1api.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResourceRequest {

    @NotNull
    @NotBlank
    private String title;

    @NotNull
    @NotBlank
    private String name;

}
