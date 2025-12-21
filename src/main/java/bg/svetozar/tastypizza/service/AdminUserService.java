package bg.svetozar.tastypizza.service;

import bg.svetozar.tastypizza.exception.*;
import bg.svetozar.tastypizza.model.dto.admin.AdminUserDto;
import bg.svetozar.tastypizza.model.entity.User;
import bg.svetozar.tastypizza.model.enums.UserRole;
import bg.svetozar.tastypizza.repository.UserRepository;
import bg.svetozar.tastypizza.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;

    public Page<AdminUserDto> list(String q, String show, Pageable pageable) {
        String s = normalizeShow(show);
        return userRepository.adminSearch(q, s, pageable).map(this::toDto);
    }

    @Transactional
    public AdminUserDto changeRole(Long id, UserRole newRole) {
        validateId(id);
        validateRole(newRole);

        Long currentUserId = getCurrentUserId();
        if (currentUserId != null && currentUserId.equals(id)) {
            throw new ForbiddenException(
                    "You cannot change your own role.",
                    ErrorCode.ADMIN_CANNOT_CHANGE_OWN_ROLE,
                    ErrorContext.of("userId", id)
            );
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        "User not found.",
                        ErrorCode.USER_NOT_FOUND,
                        ErrorContext.of("userId", id)
                ));

        if (user.isDeleted()) {
            throw new ConflictException(
                    "User is deleted and cannot be modified.",
                    ErrorCode.USER_DELETED,
                    ErrorContext.of("userId", id)
            );
        }

        if (user.getRole() != newRole) {
            user.setRole(newRole);
            user.setTokenVersion(user.getTokenVersion() + 1);
        }

        return toDto(user);
    }

    @Transactional
    public AdminUserDto softDelete(Long id) {
        validateId(id);

        Long currentUserId = getCurrentUserId();
        if (currentUserId != null && currentUserId.equals(id)) {
            throw new ForbiddenException(
                    "You cannot delete your own account.",
                    ErrorCode.ADMIN_CANNOT_DELETE_SELF,
                    ErrorContext.of("userId", id)
            );
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        "User not found.",
                        ErrorCode.USER_NOT_FOUND,
                        ErrorContext.of("userId", id)
                ));

        if (user.isDeleted()) {
            // idempotent
            return toDto(user);
        }

        user.setDeleted(true);
        user.setDeletedAt(LocalDateTime.now());
        user.setTokenVersion(user.getTokenVersion() + 1);

        return toDto(user);
    }

    @Transactional
    public AdminUserDto restore(Long id) {
        validateId(id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        "User not found.",
                        ErrorCode.USER_NOT_FOUND,
                        ErrorContext.of("userId", id)
                ));

        if (!user.isDeleted()) {
            // idempotent
            return toDto(user);
        }

        user.setDeleted(false);
        user.setDeletedAt(null);
        user.setTokenVersion(user.getTokenVersion() + 1);

        return toDto(user);
    }

    private void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new BadRequestException(
                    "Invalid user id.",
                    ErrorCode.INVALID_USER_ID,
                    ErrorContext.of("userId", id)
            );
        }
    }

    private void validateRole(UserRole role) {
        if (role == null) {
            throw new BadRequestException(
                    "Role is required.",
                    ErrorCode.ROLE_REQUIRED
            );
        }
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
    private String normalizeShow(String show) {
        String s = (show == null || show.isBlank())
                ? "active"
                : show.trim().toLowerCase();

        if (!s.equals("active") && !s.equals("deleted") && !s.equals("all")) {
            throw new BadRequestException(
                    "Invalid show filter. Allowed: active, deleted, all.",
                    ErrorCode.INVALID_SHOW_FILTER,
                    ErrorContext.of("show", show)
            );
        }
        return s;
    }
}
