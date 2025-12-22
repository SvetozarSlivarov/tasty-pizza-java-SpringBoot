package bg.svetozar.tastypizza.controller;

import bg.svetozar.tastypizza.model.dto.admin.AdminOrderStatusUpdateDto;
import bg.svetozar.tastypizza.service.AdminOrderService;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
@Validated
public class OrderAdminActionsController {

    private final AdminOrderService adminOrderService;

    @PostMapping("/{id}/start-preparing")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<AdminOrderStatusUpdateDto> startPreparing(
            @PathVariable @Positive(message = "id must be positive") Long id
    ) {
        return ResponseEntity.ok(adminOrderService.adminStartPreparing(id));
    }

    @PostMapping("/{id}/out-for-delivery")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<AdminOrderStatusUpdateDto> outForDelivery(
            @PathVariable @Positive(message = "id must be positive") Long id
    ) {
        return ResponseEntity.ok(adminOrderService.adminOutForDelivery(id));
    }

    @PostMapping("/{id}/deliver")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<AdminOrderStatusUpdateDto> deliver(
            @PathVariable @Positive(message = "id must be positive") Long id
    ) {
        return ResponseEntity.ok(adminOrderService.adminDeliver(id));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<AdminOrderStatusUpdateDto> cancel(
            @PathVariable @Positive(message = "id must be positive") Long id
    ) {
        return ResponseEntity.ok(adminOrderService.adminCancel(id));
    }
}
