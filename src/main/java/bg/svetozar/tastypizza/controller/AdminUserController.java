package bg.svetozar.tastypizza.controller;

import bg.svetozar.tastypizza.model.dto.admin.AdminUserDto;
import bg.svetozar.tastypizza.model.dto.admin.UpdateUserRoleRequest;
import bg.svetozar.tastypizza.service.AdminUserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Validated
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Page<AdminUserDto> list(
            @RequestParam(name = "q", required = false) String query,
            @RequestParam(name = "show", required = false, defaultValue = "active") String visibility,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "page must be >= 0") int page,
            @RequestParam(defaultValue = "20") @Min(value = 1, message = "size must be >= 1")
            @Max(value = 200, message = "size must be <= 200") int size
    ) {
        return adminUserService.list(query, visibility, PageRequest.of(page, size, Sort.by("id").descending()));
    }

    @PatchMapping("/{id}/role")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public AdminUserDto changeRole(@PathVariable Long id, @RequestBody @Valid UpdateUserRoleRequest req) {
        return adminUserService.changeRole(id, req.role());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public AdminUserDto softDelete(@PathVariable Long id) {
        return adminUserService.softDelete(id);
    }

    @PostMapping("/{id}/restore")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public AdminUserDto restore(@PathVariable Long id) {
        return adminUserService.restore(id);
    }
}
