package bg.svetozar.tastypizza.service;

import bg.svetozar.tastypizza.exception.*;
import bg.svetozar.tastypizza.model.dto.auth.LoginRequest;
import bg.svetozar.tastypizza.model.dto.auth.RegisterRequest;
import bg.svetozar.tastypizza.model.entity.User;
import bg.svetozar.tastypizza.model.enums.UserRole;
import bg.svetozar.tastypizza.repository.UserRepository;
import bg.svetozar.tastypizza.security.CustomUserDetails;
import bg.svetozar.tastypizza.security.CustomUserDetailsService;
import bg.svetozar.tastypizza.security.JwtService;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;

    @Transactional
    public Tokens register(RegisterRequest request) {
        // DTO-то трябва да е @Valid в контролера, така че тук приемаме, че request не е null.
        String username = request.getUsername();

        if (userRepository.existsByUsername(username)) {
            throw new ConflictException(
                    "Username is already taken",
                    ErrorCode.USERNAME_ALREADY_TAKEN,
                    ErrorContext.of("username", username)
            );
        }

        User user = User.builder()
                .fullname(request.getFullname())
                .username(username)
                .password(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.CUSTOMER)
                .deleted(false)
                .tokenVersion(0)
                .build();

        userRepository.save(user);

        CustomUserDetails userDetails = new CustomUserDetails(user);

        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        return new Tokens(accessToken, refreshToken);
    }

    public Tokens login(LoginRequest request) {
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                );

        try {
            var authentication = authenticationManager.authenticate(authToken);

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User user = userDetails.getUser();

            if (user.isDeleted()) {
                throw new ForbiddenException(
                        "User is deleted",
                        ErrorCode.USER_DELETED,
                        ErrorContext.of("userId", user.getId())
                );
            }

            String accessToken = jwtService.generateAccessToken(userDetails);
            String refreshToken = jwtService.generateRefreshToken(userDetails);

            return new Tokens(accessToken, refreshToken);

        } catch (AuthenticationException ex) {
            throw new UnauthorizedException(
                    "Invalid username or password"
            );
        }
    }

    public Tokens refreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new BadRequestException(
                    "Refresh token is required",
                    ErrorCode.BAD_REQUEST
            );
        }

        String username;
        try {
            username = jwtService.extractUsername(refreshToken);
        } catch (JwtException ex) {
            // не издаваме детайли
            throw new UnauthorizedException(
                    "Invalid refresh token"
            );
        }

        CustomUserDetails userDetails;
        try {
            userDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(username);
        } catch (UsernameNotFoundException ex) {
            throw new UnauthorizedException(
                    "Invalid refresh token"
            );
        }

        User user = userDetails.getUser();
        if (user != null && user.isDeleted()) {
            throw new ForbiddenException(
                    "User is deleted",
                    ErrorCode.USER_DELETED,
                    ErrorContext.of("userId", user.getId())
            );
        }

        if (!jwtService.isRefreshTokenValid(refreshToken, userDetails)) {
            throw new UnauthorizedException(
                    "Invalid refresh token"
            );
        }

        String newAccessToken = jwtService.generateAccessToken(userDetails);
        String newRefreshToken = jwtService.generateRefreshToken(userDetails);

        return new Tokens(newAccessToken, newRefreshToken);
    }

    public record Tokens(String accessToken, String refreshToken) {}
}
