package kr.com.mfa.mfaphase1api.client;

import kr.com.mfa.mfaphase1api.model.dto.request.UserIdsRequest;
import kr.com.mfa.mfaphase1api.model.dto.response.APIResponse;
import kr.com.mfa.mfaphase1api.model.dto.response.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "authentication", path = "/api/v1/users")
public interface UserClient {

    @GetMapping("/{user-id}")
    ResponseEntity<APIResponse<UserResponse>> getUserInfoById(@PathVariable("user-id") UUID userId);

    @PostMapping("/by-user-ids")
    ResponseEntity<APIResponse<List<UserResponse>>> getAllUserByUserIds(@RequestBody UserIdsRequest request);

}
