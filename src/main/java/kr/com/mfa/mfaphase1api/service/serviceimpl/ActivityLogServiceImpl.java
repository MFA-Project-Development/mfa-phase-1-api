package kr.com.mfa.mfaphase1api.service.serviceimpl;

import jakarta.servlet.http.HttpServletRequest;
import kr.com.mfa.mfaphase1api.model.entity.ActivityLog;
import kr.com.mfa.mfaphase1api.repository.ActivityLogRepository;
import kr.com.mfa.mfaphase1api.service.ActivityLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@Slf4j
@RequiredArgsConstructor
public class ActivityLogServiceImpl implements ActivityLogService {

    private final ActivityLogRepository activityLogRepository;

    @Override
    public void saveOrUpdateActivityLog(String actor, String action, String method, String path,
                                        int status, String detail, HttpServletRequest request) {

        Instant now = Instant.now();
        boolean success = status < 400;

        ActivityLog activityLog = activityLogRepository.findByActor(actor)
                .orElseGet(() -> ActivityLog.builder()
                        .actor(actor)
                        .build());

        activityLog.setAction(action);
        activityLog.setMethod(method);
        activityLog.setPath(path);
        activityLog.setStatus(status);
        activityLog.setSuccess(success);
        activityLog.setIpAddress(getClientIp(request));
        activityLog.setUserAgent(request.getHeader("User-Agent"));
        activityLog.setDetail(detail);
        activityLog.setCreatedAt(now);

        activityLogRepository.save(activityLog);
    }

    private String getClientIp(HttpServletRequest request) {

        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null && !xfHeader.isBlank()) {
            return xfHeader.split(",")[0];
        }
        return request.getRemoteAddr();
    }
}
