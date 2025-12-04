package bg.svetozar.tastypizza.service;

import bg.svetozar.tastypizza.exception.UsernameAlreadyTakenException;
import bg.svetozar.tastypizza.model.dto.user.UpdateUserRequest;
import bg.svetozar.tastypizza.model.dto.user.UserDto;
import bg.svetozar.tastypizza.model.entity.User;
import bg.svetozar.tastypizza.model.mapper.UserMapper;
import bg.svetozar.tastypizza.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private User getCurrentUserOrThrow() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("User must be authenticated");
        }

        String username = auth.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found: " + username));
    }

    @Transactional(readOnly = true)
    public UserDto getProfile() {
        return UserMapper.toDto(getCurrentUserOrThrow());
    }

    @Transactional
    public UserDto updateProfile(UpdateUserRequest request) {
        User user = getCurrentUserOrThrow();

        if (request.fullname() != null && !request.fullname().isBlank()) {
            user.setFullname(request.fullname());
        }

        if (request.username() != null && !request.username().isBlank()
                && !request.username().equals(user.getUsername())) {

            if (userRepository.existsByUsername(request.username())) {
                throw new UsernameAlreadyTakenException(request.username());
            }

            user.setUsername(request.username());
        }

        userRepository.save(user);
        return UserMapper.toDto(user);
    }
}
