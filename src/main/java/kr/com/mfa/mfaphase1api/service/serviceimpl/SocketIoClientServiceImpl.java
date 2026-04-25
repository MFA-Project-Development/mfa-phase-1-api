package kr.com.mfa.mfaphase1api.service.serviceimpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.socket.client.IO;
import io.socket.client.Socket;
import jakarta.annotation.PostConstruct;
import kr.com.mfa.mfaphase1api.exception.InternalException;
import kr.com.mfa.mfaphase1api.model.dto.response.AssessmentMessage;
import kr.com.mfa.mfaphase1api.service.SocketIoClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URISyntaxException;

@Slf4j
@Service
@RequiredArgsConstructor
public class SocketIoClientServiceImpl implements SocketIoClientService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${socketio.server.url}")
    private String serverUrl;

    private Socket socket;

    @PostConstruct
    public void init() {
        connect();
    }

    private synchronized void connect() {
        if (socket != null && socket.connected()) {
            return;
        }

        try {
            IO.Options options = IO.Options.builder()
                    .setTransports(new String[]{"websocket"})
                    .build();

            socket = IO.socket(serverUrl, options);

            socket.on(Socket.EVENT_CONNECT, args ->
                    log.info("Socket.IO connected to {}", serverUrl));

            socket.on(Socket.EVENT_CONNECT_ERROR, args ->
                    log.warn("Socket.IO connection error to {}: {}", serverUrl,
                            args.length > 0 ? args[0] : "unknown"));

            socket.on(Socket.EVENT_DISCONNECT, args ->
                    log.warn("Socket.IO disconnected from {}: {}", serverUrl,
                            args.length > 0 ? args[0] : "unknown"));

            socket.connect();

        } catch (URISyntaxException e) {
            throw new InternalException("Failed to connect to socket server: " + serverUrl);
        }
    }

    @Override
    public void emitAssessmentStatus(AssessmentMessage message) {
        connect();

        if (socket == null || !socket.connected()) {
            log.warn("Socket.IO not connected — dropping assessment-status event for assessmentId={}. " +
                    "Server: {}", message.getAssessmentId(), serverUrl);
            return;
        }

        try {
            String json = objectMapper.writeValueAsString(message);
            socket.emit("assessment-status", json);
            log.debug("Emitted assessment-status for assessmentId={}", message.getAssessmentId());
        } catch (JsonProcessingException e) {
            throw new InternalException("Failed to serialize assessment status: " + message);
        }
    }
}
