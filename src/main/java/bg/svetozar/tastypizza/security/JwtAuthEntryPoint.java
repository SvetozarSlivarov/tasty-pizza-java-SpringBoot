package bg.svetozar.tastypizza.security;

import bg.svetozar.tastypizza.exception.ApiError;
import bg.svetozar.tastypizza.exception.ErrorCode;
import bg.svetozar.tastypizza.exception.ErrorMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;

@Component
public class JwtAuthEntryPoint implements AuthenticationEntryPoint {

    public static final String APPLICATION_JSON_UTF8 = "application/json;charset=UTF-8";

    private final ObjectMapper objectMapper;

    public JwtAuthEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(APPLICATION_JSON_UTF8);

        String message = resolveMessage(authException);
        String traceId = resolveTraceId(request);

        ApiError body = new ApiError(
                Instant.now(),
                401,
                "Unauthorized",
                message,
                request.getRequestURI(),
                ErrorCode.UNAUTHORIZED,
                traceId,
                Map.of(),
                null
        );

        objectMapper.writeValue(response.getOutputStream(), body);
    }

    private String resolveMessage(AuthenticationException ex) {
        return ErrorMessage.REQUIRED_AUTHENTICATION;
    }

    private String resolveTraceId(HttpServletRequest request) {
        return null;
    }
}

