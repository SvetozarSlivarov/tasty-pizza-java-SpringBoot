package bg.svetozar.tastypizza.service;

import bg.svetozar.tastypizza.exception.InvalidCredentialsException;
import bg.svetozar.tastypizza.exception.UsernameAlreadyTakenException;
import bg.svetozar.tastypizza.model.dto.auth.AuthDto;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;

    public AuthDto register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UsernameAlreadyTakenException(request.getUsername());
        }

        User user = User.builder()
                .fullname(request.getFullname())
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.CUSTOMER)
                .build();

        userRepository.save(user);

        CustomUserDetails userDetails = new CustomUserDetails(user);

        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        return new AuthDto(
                accessToken,
                refreshToken,
                user.getUsername(),
                user.getRole().name()
        );
    }


    public AuthDto login(LoginRequest request) {
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                );

        try {
            var authentication = authenticationManager.authenticate(authToken);

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User user = userDetails.getUser();

            String accessToken = jwtService.generateAccessToken(userDetails);
            String refreshToken = jwtService.generateRefreshToken(userDetails);

            return new AuthDto(
                    accessToken,
                    refreshToken,
                    user.getUsername(),
                    user.getRole().name()
            );

        } catch (AuthenticationException ex) {
            throw new InvalidCredentialsException();
        }
    }

    public AuthDto refreshToken(String refreshToken) {
        String username;
        try {
            username = jwtService.extractUsername(refreshToken);
        } catch (JwtException ex) {
            throw new InvalidCredentialsException();
        }

        CustomUserDetails userDetails =
                (CustomUserDetails) userDetailsService.loadUserByUsername(username);
        User user = userDetails.getUser();

        if (!jwtService.isRefreshTokenValid(refreshToken, userDetails)) {
            throw new InvalidCredentialsException();
        }

        String newAccessToken = jwtService.generateAccessToken(userDetails);
        String newRefreshToken = jwtService.generateRefreshToken(userDetails);

        return new AuthDto(
                newAccessToken,
                newRefreshToken,
                user.getUsername(),
                user.getRole().name()
        );
    }
}
