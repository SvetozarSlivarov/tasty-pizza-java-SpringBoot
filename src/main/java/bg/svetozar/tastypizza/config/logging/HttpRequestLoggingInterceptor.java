package bg.svetozar.tastypizza.config.logging;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class HttpRequestLoggingInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(HttpRequestLoggingInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute("startNs", System.nanoTime());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        long durationMs = 0;
        Object startObj = request.getAttribute("startNs");
        if (startObj instanceof Long startNs) {
            durationMs = (System.nanoTime() - startNs) / 1_000_000;
        }

        log.info("HTTP {} {} -> {} ({}ms) traceId={}",
                request.getMethod(),
                request.getRequestURI(),
                response.getStatus(),
                durationMs,
                MDC.get("traceId"));
    }
}
