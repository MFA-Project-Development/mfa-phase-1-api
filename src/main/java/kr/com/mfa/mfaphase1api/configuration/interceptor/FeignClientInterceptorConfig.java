package kr.com.mfa.mfaphase1api.configuration.interceptor;

import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Configuration
public class FeignClientInterceptorConfig {

    @Bean
    public RequestInterceptor feignClientInterceptor() {
        return template -> {
            String auth = getAuthorizationFromRequest();

            if (auth != null && !auth.isBlank()) {
                template.header("Authorization", auth);
            }

            template.header("X-Correlation-Id",
                    template.headers().getOrDefault("X-Correlation-Id",
                                    java.util.List.of(java.util.UUID.randomUUID().toString()))
                            .toArray(new String[0]));
        };
    }

    private String getAuthorizationFromRequest() {
        var attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) return null;
        HttpServletRequest req = attrs.getRequest();
        return req.getHeader("Authorization");
    }
}