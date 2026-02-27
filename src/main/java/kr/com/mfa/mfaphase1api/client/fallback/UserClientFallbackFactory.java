package kr.com.mfa.mfaphase1api.client.fallback;

import kr.com.mfa.mfaphase1api.client.UserClient;
import kr.com.mfa.mfaphase1api.model.dto.request.UserIdsRequest;
import kr.com.mfa.mfaphase1api.model.dto.response.APIResponse;
import kr.com.mfa.mfaphase1api.model.dto.response.UserResponse;
import kr.com.mfa.mfaphase1api.model.enums.BaseRole;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class UserClientFallbackFactory implements FallbackFactory<UserClient> {

    @Override
    public UserClient create(Throwable cause) {

        log.error("UserClient fallback triggered. Reason: {}", cause.getMessage(), cause);

        return new UserClient() {

            @Override
            public ResponseEntity<APIResponse<UserResponse>> getUserInfoById(UUID userId) {
                return ResponseEntity
                        .status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body(APIResponse.<UserResponse>builder()
                                .message("User service unavailable (fallback)")
                                .payload(null)
                                .instant(Instant.now())
                                .status(HttpStatus.SERVICE_UNAVAILABLE)
                                .build());
            }

            @Override
            public ResponseEntity<APIResponse<List<UserResponse>>> getAllUserByUserIds(UserIdsRequest request) {
                return ResponseEntity
                        .status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body(APIResponse.<List<UserResponse>>builder()
                                .message("User service unavailable (fallback)")
                                .payload(Collections.emptyList())
                                .instant(Instant.now())
                                .status(HttpStatus.SERVICE_UNAVAILABLE)
                                .build());
            }

            @Override
            public ResponseEntity<APIResponse<List<?>>> getAllUsersBaseRole(BaseRole baseRole) {
                return ResponseEntity
                        .status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body(APIResponse.<List<?>>builder()
                                .message("User service unavailable (fallback)")
                                .payload(Collections.emptyList())
                                .instant(Instant.now())
                                .status(HttpStatus.SERVICE_UNAVAILABLE)
                                .build());
            }
        };
    }
}