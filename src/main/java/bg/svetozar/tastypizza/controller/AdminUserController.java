package bg.svetozar.tastypizza.controller;

import bg.svetozar.tastypizza.model.dto.admin.AdminUserDto;
import bg.svetozar.tastypizza.model.dto.admin.UpdateUserRoleRequest;
import bg.svetozar.tastypizza.model.enums.UserRole;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import bg.svetozar.tastypizza.service.AdminUserService;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public Page<AdminUserDto> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false, defaultValue = "active") String show,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return adminUserService.list(q, show, PageRequest.of(page, size, Sort.by("id").descending()));
    }

    @PatchMapping("/{id}/role")
    public AdminUserDto changeRole(@PathVariable Long id, @RequestBody @Valid UpdateUserRoleRequest req) {
        return adminUserService.changeRole(id, req.role());
    }

    @DeleteMapping("/{id}")
    public AdminUserDto softDelete(@PathVariable Long id) {
        return adminUserService.softDelete(id);
    }

    @PostMapping("/{id}/restore")
    public AdminUserDto restore(@PathVariable Long id) {
        return adminUserService.restore(id);
    }
}
