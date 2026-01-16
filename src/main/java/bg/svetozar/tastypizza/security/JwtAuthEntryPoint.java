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

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

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

        response.getWriter().write(objectMapper.writeValueAsString(body));
    }

    private String resolveMessage(AuthenticationException ex) {
        return ErrorMessage.REQUIRED_AUTHENTICATION;
    }

    private String resolveTraceId(HttpServletRequest request) {
        return null;
    }
}
