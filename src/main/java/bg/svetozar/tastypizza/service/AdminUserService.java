package bg.svetozar.tastypizza.service;

import bg.svetozar.tastypizza.exception.BusinessException;
import bg.svetozar.tastypizza.exception.ErrorCode;
import bg.svetozar.tastypizza.exception.ForbiddenException;
import bg.svetozar.tastypizza.exception.NotFoundException;
import bg.svetozar.tastypizza.model.dto.admin.AdminUserDto;
import bg.svetozar.tastypizza.model.entity.User;
import bg.svetozar.tastypizza.model.enums.UserRole;
import bg.svetozar.tastypizza.repository.UserRepository;
import bg.svetozar.tastypizza.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;

    public Page<AdminUserDto> list(String q, String show, Pageable pageable) {
        String s = (show == null || show.isBlank()) ? "active" : show;
        return userRepository.adminSearch(q, s, pageable).map(this::toDto);
    }

    @Transactional
    public AdminUserDto changeRole(Long id, UserRole newRole) {
        Long currentUserId = getCurrentUserId();
        if (currentUserId != null && currentUserId.equals(id)) {
            throw new ForbiddenException("You cannot change your own role");
        }

        User user = userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("User not found"));



        if (user.getRole() != newRole) {
            user.setRole(newRole);

            user.setTokenVersion(user.getTokenVersion() + 1);
        }

        return toDto(user);
    }

    @Transactional
    public AdminUserDto softDelete(Long id) {

        Long currentUserId = getCurrentUserId();
        if (currentUserId != null && currentUserId.equals(id)) {
            throw new ForbiddenException("You cannot delete your own account");
        }

        User user = userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("User not found"));

        user.setDeleted(true);
        user.setDeletedAt(LocalDateTime.now());

        user.setTokenVersion(user.getTokenVersion() + 1);

        return toDto(user);
    }

    @Transactional
    public AdminUserDto restore(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (user.isDeleted()) {
            user.setDeleted(false);
            user.setDeletedAt(null);

            user.setTokenVersion(user.getTokenVersion() + 1);
        }

        return toDto(user);
    }

    private AdminUserDto toDto(User u) {
        return new AdminUserDto(
                u.getId(),
                u.getUsername(),
                u.getFullname(),
                u.getRole(),
                u.isDeleted(),
                u.getDeletedAt(),
                u.getTokenVersion(),
                u.getCreatedAt()
        );
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }

        Object principal = auth.getPrincipal();
        if (principal instanceof CustomUserDetails cud) {
            return cud.getUser().getId();
        }

        return null;
    }
}
