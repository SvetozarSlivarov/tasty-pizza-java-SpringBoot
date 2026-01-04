package bg.svetozar.tastypizza.controller;

import bg.svetozar.tastypizza.service.GuestCartCleanupService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/maintenance")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminMaintenanceController {

    private final GuestCartCleanupService guestCartCleanupService;

    @PostMapping("/guest-carts/cleanup")
    public Map<String, Long> cleanupGuestCarts() {
        long deleted = guestCartCleanupService.cleanupOnce();
        return Map.of("deleted", deleted);
    }
}
