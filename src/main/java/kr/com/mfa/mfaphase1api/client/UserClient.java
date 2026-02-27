package kr.com.mfa.mfaphase1api.client;

import kr.com.mfa.mfaphase1api.client.fallback.UserClientFallbackFactory;
import kr.com.mfa.mfaphase1api.configuration.interceptor.FeignClientInterceptorConfig;
import kr.com.mfa.mfaphase1api.model.dto.request.UserIdsRequest;
import kr.com.mfa.mfaphase1api.model.dto.response.APIResponse;
import kr.com.mfa.mfaphase1api.model.dto.response.UserResponse;
import kr.com.mfa.mfaphase1api.model.enums.BaseRole;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@FeignClient(
        name = "authentication",
        path = "/api/v1/users",
        fallbackFactory = UserClientFallbackFactory.class,
        configuration = FeignClientInterceptorConfig.class
)
public interface UserClient {

    @GetMapping("/{user-id}")
    ResponseEntity<APIResponse<UserResponse>> getUserInfoById(@PathVariable("user-id") UUID userId);

    @PostMapping("/by-user-ids")
    ResponseEntity<APIResponse<List<UserResponse>>> getAllUserByUserIds(@RequestBody UserIdsRequest request);

    @GetMapping("/base-role")
    ResponseEntity<APIResponse<List<?>>> getAllUsersBaseRole(@RequestParam BaseRole baseRole);

}
