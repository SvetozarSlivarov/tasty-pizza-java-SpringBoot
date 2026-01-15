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
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;

    public Page<AdminUserDto> list(String q, String show, Pageable pageable) {
        String showFilter = normalizeShow(show);
        return userRepository.adminSearch(q, showFilter, pageable).map(this::toDto);
    }

    @Transactional
    public AdminUserDto changeRole(Long id, UserRole newRole) {
        validateId(id);
        validateRole(newRole);

        Long currentUserId = getCurrentUserId();
        if (currentUserId != null && currentUserId.equals(id)) {
            throw new ForbiddenException(
                    ErrorMessage.ADMIN_CANNOT_CHANGE_OWN_ROLE,
                    ErrorCode.ADMIN_CANNOT_CHANGE_OWN_ROLE,
                    ErrorContext.of("userId", id)
            );
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        ErrorMessage.USER_NOT_FOUND,
                        ErrorCode.USER_NOT_FOUND,
                        ErrorContext.of("userId", id)
                ));

        if (user.isDeleted()) {
            throw new ConflictException(
                    ErrorMessage.USER_IS_DELETED_CANNOT_MODIFIED,
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
                    ErrorMessage.ADMIN_CANNOT_DELETE_SELF,
                    ErrorCode.ADMIN_CANNOT_DELETE_SELF,
                    ErrorContext.of("userId", id)
            );
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        ErrorMessage.USER_NOT_FOUND,
                        ErrorCode.USER_NOT_FOUND,
                        ErrorContext.of("userId", id)
                ));

        if (user.isDeleted()) {
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
                        ErrorMessage.USER_NOT_FOUND,
                        ErrorCode.USER_NOT_FOUND,
                        ErrorContext.of("userId", id)
                ));

        if (!user.isDeleted()) {
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
                    ErrorMessage.INVALID_USER_ID,
                    ErrorCode.INVALID_USER_ID,
                    ErrorContext.of("userId", id)
            );
        }
    }

    private void validateRole(UserRole role) {
        if (role == null) {
            throw new BadRequestException(
                    ErrorMessage.ROLE_REQUIRED,
                    ErrorCode.ROLE_REQUIRED
            );
        }
    }

    private AdminUserDto toDto(User user) {
        return new AdminUserDto(
                user.getId(),
                user.getUsername(),
                user.getFullname(),
                user.getRole(),
                user.isDeleted(),
                user.getDeletedAt(),
                user.getTokenVersion(),
                user.getCreatedAt()
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
        String showFilter = (!StringUtils.hasText(show))
                ? "active"
                : show.trim().toLowerCase();

        if (!showFilter.equals("active") && !showFilter.equals("deleted") && !showFilter.equals("all")) {
            throw new BadRequestException(
                    ErrorMessage.INVALID_SHOW_FILTER,
                    ErrorCode.INVALID_SHOW_FILTER,
                    ErrorContext.of("show", show)
            );
        }
        return showFilter;
    }
}
