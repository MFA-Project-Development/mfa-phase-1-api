package kr.com.mfa.mfaphase1api.configuration.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.UUID;

@Configuration
public class FeignClientInterceptorConfig {

    @Bean
    public RequestInterceptor feignClientInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate request) {
                if (!request.headers().containsKey("Authorization")) {
                    String token = resolveTokenFromSecurityContext();
                    if (token != null && !token.isBlank()) {
                        request.header("Authorization", "Bearer " + token);
                    }
                }

                request.header("X-Correlation-Id",
                        request.headers().getOrDefault("X-Correlation-Id",
                                        java.util.List.of(UUID.randomUUID().toString()))
                                .toArray(new String[0])
                );
            }
        };
    }

    private String resolveTokenFromSecurityContext() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;

        if (auth instanceof JwtAuthenticationToken jwtAuth && jwtAuth.getToken() != null) {
            return jwtAuth.getToken().getTokenValue();
        }

        return null;
    }
}

