package kr.com.mfa.mfaphase1api.model.dto.response;

import lombok.*;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaperResponse {
    private UUID paperId;
    private Integer page;
    private String name;
    private UUID submissionId;
}
