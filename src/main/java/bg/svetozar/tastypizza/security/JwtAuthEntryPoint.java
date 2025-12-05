package bg.svetozar.tastypizza.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.OffsetDateTime;

@Component
public class JwtAuthEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
        response.setContentType("application/json;charset=UTF-8");

        String message = authException.getMessage();
        if (message == null || message.isBlank()) {
            message = "Authentication required";
        }

        String body = """
            {
              "timestamp": "%s",
              "status": 401,
              "error": "Unauthorized",
              "message": "%s",
              "path": "%s",
              "code": "UNAUTHORIZED",
              "validationErrors": null
            }
            """.formatted(
                OffsetDateTime.now().toString(),
                message,
                request.getRequestURI()
        );

        response.getWriter().write(body);
    }
}
