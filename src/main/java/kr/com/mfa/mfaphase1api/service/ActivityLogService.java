package kr.com.mfa.mfaphase1api.service;

import jakarta.servlet.http.HttpServletRequest;

public interface ActivityLogService {

    void saveOrUpdateActivityLog(String actor, String action, String method, String path, int status, String detail, HttpServletRequest request);

}
