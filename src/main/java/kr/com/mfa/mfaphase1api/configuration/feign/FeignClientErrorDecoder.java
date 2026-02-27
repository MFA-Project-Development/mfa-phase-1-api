package kr.com.mfa.mfaphase1api.configuration.feign;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.Util;
import feign.codec.ErrorDecoder;
import kr.com.mfa.mfaphase1api.exception.BadRequestException;
import kr.com.mfa.mfaphase1api.exception.ConflictException;
import kr.com.mfa.mfaphase1api.exception.NotFoundException;
import kr.com.mfa.mfaphase1api.exception.UnauthorizeException;

import java.io.Reader;
import java.nio.charset.StandardCharsets;

public class FeignClientErrorDecoder implements ErrorDecoder {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public Exception decode(String methodKey, Response response) {
        String msg = "Unknown error";

        try {
            String body = readBody(response);
            if (body != null && !body.isBlank()) {
                JsonNode node = mapper.readTree(body);
                if (node.has("detail")) msg = node.get("detail").asText();
                else msg = body;
            }
        } catch (Exception e) {
            msg = "Error reading response body";
        }

        String url = response.request() != null ? response.request().url() : "unknown-url";
        String fullMsg = "[Feign] " + methodKey + " -> " + response.status() + " " + url + " | " + msg;

        return switch (response.status()) {
            case 400 -> new BadRequestException(fullMsg);
            case 401 -> new UnauthorizeException(fullMsg);
            case 404 -> new NotFoundException(fullMsg);
            case 409 -> new ConflictException(fullMsg);
            default -> new RuntimeException(fullMsg);
        };
    }

    private static String readBody(Response response) {
        if (response.body() == null) return null;
        try (Reader reader = response.body().asReader(StandardCharsets.UTF_8)) {
            String s = Util.toString(reader);
            return s.length() > 8192 ? s.substring(0, 8192) + "…(truncated)" : s;
        } catch (Exception ignored) {
            return null;
        }
    }
}
