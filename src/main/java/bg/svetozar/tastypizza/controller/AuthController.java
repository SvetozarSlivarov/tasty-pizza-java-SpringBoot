package bg.svetozar.tastypizza.controller;

import static bg.svetozar.tastypizza.exception.ErrorMessage.INVALID_REFRESH_TOKEN;
import bg.svetozar.tastypizza.exception.UnauthorizedException;
import bg.svetozar.tastypizza.model.dto.auth.AuthResponse;
import bg.svetozar.tastypizza.model.dto.auth.LoginRequest;
import bg.svetozar.tastypizza.model.dto.auth.RegisterRequest;
import bg.svetozar.tastypizza.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final String REFRESH_COOKIE = "refreshToken";

    private final AuthService authService;

    @Value("${app.jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    @Value("${app.cookies.secure:false}")
    private boolean cookieSecure;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletResponse response
    ) {
        var tokens = authService.register(request);
        setRefreshCookie(response, tokens.refreshToken());
        return ResponseEntity.ok(new AuthResponse(tokens.accessToken()));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response
    ) {
        var tokens = authService.login(request);
        setRefreshCookie(response, tokens.refreshToken());
        return ResponseEntity.ok(new AuthResponse(tokens.accessToken()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @CookieValue(name = REFRESH_COOKIE, required = false) String refreshToken,
            HttpServletResponse response
    ) {
        if (!StringUtils.hasText(refreshToken)) {
            throw new UnauthorizedException(
                    INVALID_REFRESH_TOKEN,
                    Map.of()
            );
        }

        var tokens = authService.refreshToken(refreshToken);
        setRefreshCookie(response, tokens.refreshToken());
        return ResponseEntity.ok(new AuthResponse(tokens.accessToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        clearRefreshCookie(response);
        return ResponseEntity.noContent().build();
    }

    private void setRefreshCookie(HttpServletResponse response, String token) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_COOKIE, token)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ofMillis(refreshExpirationMs))
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void clearRefreshCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_COOKIE, "")
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Lax")
                .path("/")
                .maxAge(0)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
