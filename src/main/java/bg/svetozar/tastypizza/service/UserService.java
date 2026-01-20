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
import org.springframework.util.StringUtils;

import static bg.svetozar.tastypizza.exception.ErrorMessage.INVALID_CURRENT_PASSWORD;
import static bg.svetozar.tastypizza.exception.ErrorMessage.NOT_AUTHENTICATED;
import static bg.svetozar.tastypizza.exception.ErrorMessage.USERNAME_ALREADY_TAKEN;
import static bg.svetozar.tastypizza.exception.ErrorMessage.USER_NOT_FOUND;

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
                    NOT_AUTHENTICATED,
                    ErrorCode.UNAUTHORIZED
            );
        }

        String username = auth.getName();

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UnauthorizedException(
                        USER_NOT_FOUND,
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

        String fullname = request.fullname();
        if (StringUtils.hasText(fullname)) {
            user.setFullname(fullname.trim());
        }

        String username = request.username();
        if (StringUtils.hasText(username)) {
            String normalizedUsername = username.trim();

            if (!normalizedUsername.equals(user.getUsername())) {
                if (userRepository.existsByUsername(normalizedUsername)) {
                    throw new ConflictException(
                            USERNAME_ALREADY_TAKEN,
                            ErrorCode.USERNAME_ALREADY_TAKEN,
                            ErrorContext.of("username", normalizedUsername)
                    );
                }

                user.setUsername(normalizedUsername);
                user.setTokenVersion(user.getTokenVersion() + 1);
            }
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
                    USERNAME_ALREADY_TAKEN,
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
                    INVALID_CURRENT_PASSWORD,
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
