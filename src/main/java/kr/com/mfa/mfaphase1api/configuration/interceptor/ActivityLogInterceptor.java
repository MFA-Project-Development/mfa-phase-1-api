package kr.com.mfa.mfaphase1api.configuration.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.com.mfa.mfaphase1api.model.annotation.AuditAction;
import kr.com.mfa.mfaphase1api.service.ActivityLogService;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class ActivityLogInterceptor implements HandlerInterceptor {

    private final ActivityLogService activityLogService;

    @Override
    public void afterCompletion(HttpServletRequest request,
                                @NonNull HttpServletResponse response,
                                @NonNull Object handler,
                                @Nullable Exception ex) {

        String path = request.getRequestURI();

        if (path.startsWith("/swagger")
            || path.startsWith("/v3/api-docs")
            || path.startsWith("/actuator")
            || path.startsWith("/error")) {
            return;
        }

        String method = request.getMethod();
        if (!("POST".equals(method) || "PUT".equals(method) || "DELETE".equals(method))) {
            return;
        }

        if (!(handler instanceof HandlerMethod hm)) {
            return;
        }

        AuditAction ann = hm.getMethodAnnotation(AuditAction.class);
        if (ann == null) {
            return;
        }

        String actor = resolveActor();
        String action = resolveAction(hm, ann);
        int status = response.getStatus();
        String detail = ex != null ? safeMsg(ex.getMessage()) : null;

        activityLogService.saveOrUpdateActivityLog(
                actor,
                action,
                method,
                path,
                status,
                detail,
                request
        );
    }

    private String resolveActor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return "anonymous";
        return auth.getName();
    }

    private String resolveAction(HandlerMethod hm, AuditAction ann) {
        if (ann.value() != null && !ann.value().isBlank()) {
            return ann.value().trim();
        }
        return hm.getBeanType().getSimpleName() + "#" + hm.getMethod().getName();
    }

    private String safeMsg(String msg) {
        if (msg == null) return null;
        msg = msg.replaceAll("[\\r\\n]", " ");
        return msg.length() > 300 ? msg.substring(0, 300) : msg;
    }
}