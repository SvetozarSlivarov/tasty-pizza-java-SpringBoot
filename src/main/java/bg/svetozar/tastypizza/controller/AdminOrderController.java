package bg.svetozar.tastypizza.controller;

import bg.svetozar.tastypizza.model.dto.admin.AdminOrderDetailDto;
import bg.svetozar.tastypizza.model.dto.admin.AdminOrderListDto;
import bg.svetozar.tastypizza.model.dto.admin.AdminOrderPageDto;
import bg.svetozar.tastypizza.service.AdminOrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/orders")
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrderController {

    private final AdminOrderService adminOrderService;

    public AdminOrderController(AdminOrderService adminOrderService) {
        this.adminOrderService = adminOrderService;
    }

    @GetMapping
    public AdminOrderPageDto list(
            @RequestParam(defaultValue = "all") String status,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long userId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<AdminOrderListDto> page = adminOrderService.list(status, q, userId, pageable);
        return AdminOrderPageDto.from(page);
    }

    @GetMapping("/{id}")
    public AdminOrderDetailDto detail(@PathVariable Long id) {
        return adminOrderService.getDetail(id);
    }
}

