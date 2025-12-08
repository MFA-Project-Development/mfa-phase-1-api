package kr.com.mfa.mfaphase1api.service;

import kr.com.mfa.mfaphase1api.model.dto.response.AssessmentMessage;

public interface SocketIoClientService {

    void emitAssessmentStatus(AssessmentMessage message);

}
