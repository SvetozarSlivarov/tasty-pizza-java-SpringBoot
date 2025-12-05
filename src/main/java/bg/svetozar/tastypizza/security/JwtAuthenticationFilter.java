package bg.svetozar.tastypizza.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);
        String username;

        try {
            username = jwtService.extractUsername(jwt);
        } catch (ExpiredJwtException e) {
            writeUnauthorizedResponse(request, response, "AUTH_TOKEN_EXPIRED", "Token has expired");
            return;
        } catch (JwtException | IllegalArgumentException e) {
            writeUnauthorizedResponse(request, response, "AUTH_INVALID_TOKEN", "Invalid token");
            return;
        }

        if (username == null || SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        CustomUserDetails userDetails;
        try {
            userDetails = (CustomUserDetails) this.userDetailsService.loadUserByUsername(username);
        } catch (UsernameNotFoundException ex) {
            writeUnauthorizedResponse(request, response, "AUTH_INVALID_TOKEN", "Invalid token");
            return;
        }

        if (!jwtService.isAccessTokenValid(jwt, userDetails)) {
            writeUnauthorizedResponse(request, response, "AUTH_INVALID_TOKEN", "Invalid token");
            return;
        }

        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
        authToken.setDetails(
                new WebAuthenticationDetailsSource().buildDetails(request)
        );
        SecurityContextHolder.getContext().setAuthentication(authToken);

        filterChain.doFilter(request, response);
    }

    private void writeUnauthorizedResponse(HttpServletRequest request,
                                           HttpServletResponse response,
                                           String code,
                                           String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        String body = """
            {
              "timestamp": "%s",
              "status": 401,
              "error": "Unauthorized",
              "message": "%s",
              "path": "%s",
              "code": "%s",
              "validationErrors": null
            }
            """.formatted(
                java.time.OffsetDateTime.now().toString(),
                message,
                request.getRequestURI(),
                code
        );

        response.getWriter().write(body);
    }
}
