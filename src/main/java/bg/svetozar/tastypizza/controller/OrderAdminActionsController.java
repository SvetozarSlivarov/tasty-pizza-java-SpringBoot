package bg.svetozar.tastypizza.controller;

import bg.svetozar.tastypizza.model.dto.admin.AdminOrderStatusUpdateDto;
import bg.svetozar.tastypizza.service.AdminOrderService;
import bg.svetozar.tastypizza.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderAdminActionsController {

    private final AdminOrderService adminOrderService;
    private final AdminOrderService adminOrderService2;

    @PostMapping("/{id}/start-preparing")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminOrderStatusUpdateDto> startPreparing(@PathVariable Long id) {
        return ResponseEntity.ok(adminOrderService.adminStartPreparing(id));
    }

    @PostMapping("/{id}/out-for-delivery")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminOrderStatusUpdateDto> outForDelivery(@PathVariable Long id) {
        return ResponseEntity.ok(adminOrderService.adminOutForDelivery(id));
    }

    @PostMapping("/{id}/deliver")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminOrderStatusUpdateDto> deliver(@PathVariable Long id) {
        return ResponseEntity.ok(adminOrderService.adminDeliver(id));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminOrderStatusUpdateDto> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(adminOrderService.adminCancel(id));
    }
}
