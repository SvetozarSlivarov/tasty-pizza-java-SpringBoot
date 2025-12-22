package bg.svetozar.tastypizza.service;

import bg.svetozar.tastypizza.exception.*;
import bg.svetozar.tastypizza.model.dto.user.*;
import bg.svetozar.tastypizza.model.entity.User;
import bg.svetozar.tastypizza.model.mapper.UserMapper;
import bg.svetozar.tastypizza.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {


    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    private User getCurrentUserOrThrow() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()
                || "anonymousUser".equals(auth.getPrincipal())) {
            throw new UnauthorizedException(
                    "Not authenticated",
                    ErrorCode.UNAUTHORIZED
            );
        }

        String username = auth.getName();

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UnauthorizedException(
                        "User not found",
                        ErrorCode.USER_NOT_FOUND,
                        ErrorContext.of("username", username)
                ));
    }


    @Transactional(readOnly = true)
    public UserDto getProfile() {
        return UserMapper.toDto(getCurrentUserOrThrow());
    }


    @Transactional
    public UserDto updateProfile(UpdateUserRequest request) {
        User user = getCurrentUserOrThrow();

        if (request.fullname() != null && !request.fullname().isBlank()) {
            user.setFullname(request.fullname().trim());
        }

        if (request.username() != null
                && !request.username().isBlank()
                && !request.username().equals(user.getUsername())) {

            if (userRepository.existsByUsername(request.username())) {
                throw new ConflictException(
                        "Username already taken",
                        ErrorCode.USERNAME_ALREADY_TAKEN,
                        ErrorContext.of("username", request.username())
                );
            }

            user.setUsername(request.username().trim());
            user.setTokenVersion(user.getTokenVersion() + 1);
        }

        userRepository.save(user);
        return UserMapper.toDto(user);
    }

    @Transactional
    public UserDto updateFullName(UpdateFullNameRequest req) {
        User user = getCurrentUserOrThrow();
        user.setFullname(req.fullname().trim());
        userRepository.save(user);
        return UserMapper.toDto(user);
    }

    @Transactional
    public void updateUsername(UpdateUsernameRequest req) {
        User user = getCurrentUserOrThrow();
        String newUsername = req.username().trim();

        if (newUsername.equals(user.getUsername())) {
            return;
        }

        if (userRepository.existsByUsername(newUsername)) {
            throw new ConflictException(
                    "Username already taken",
                    ErrorCode.USERNAME_ALREADY_TAKEN,
                    ErrorContext.of("username", newUsername)
            );
        }

        user.setUsername(newUsername);
        user.setTokenVersion(user.getTokenVersion() + 1);

        userRepository.save(user);
    }

    @Transactional
    public void changePassword(ChangePasswordRequest req) {
        User user = getCurrentUserOrThrow();

        if (!passwordEncoder.matches(req.currentPassword(), user.getPassword())) {
            throw new BadRequestException(
                    "Invalid current password",
                    ErrorCode.INVALID_CREDENTIALS
            );
        }

        user.setPassword(passwordEncoder.encode(req.newPassword().trim()));

        if (req.logoutAll()) {
            user.setTokenVersion(user.getTokenVersion() + 1);
        }

        userRepository.save(user);
    }
}
