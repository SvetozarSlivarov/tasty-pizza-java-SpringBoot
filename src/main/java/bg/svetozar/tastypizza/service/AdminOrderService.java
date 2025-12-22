package bg.svetozar.tastypizza.service;

import bg.svetozar.tastypizza.exception.*;
import bg.svetozar.tastypizza.model.dto.admin.AdminOrderDetailDto;
import bg.svetozar.tastypizza.model.dto.admin.AdminOrderItemCustomizationDto;
import bg.svetozar.tastypizza.model.dto.admin.AdminOrderItemDto;
import bg.svetozar.tastypizza.model.dto.admin.AdminOrderListDto;
import bg.svetozar.tastypizza.model.dto.admin.AdminOrderStatusChangeDto;
import bg.svetozar.tastypizza.model.dto.admin.AdminOrderStatusUpdateDto;
import bg.svetozar.tastypizza.model.entity.Order;
import bg.svetozar.tastypizza.model.entity.OrderItem;
import bg.svetozar.tastypizza.model.entity.OrderItemCustomization;
import bg.svetozar.tastypizza.model.entity.OrderStatusChange;
import bg.svetozar.tastypizza.model.enums.OrderStatus;
import bg.svetozar.tastypizza.model.enums.ProductType;
import bg.svetozar.tastypizza.repository.OrderItemRepository;
import bg.svetozar.tastypizza.repository.OrderRepository;
import bg.svetozar.tastypizza.repository.OrderStatusChangeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminOrderService {

    private final OrderRepository orderRepository;
    private final OrderStatusChangeRepository statusChangeRepository;
    private final OrderItemRepository orderItemRepository;

    @Transactional(readOnly = true)
    public Page<AdminOrderListDto> list(String statusStr, String q, Long userId, Pageable pageable) {
        OrderStatus status = parseStatusOrThrow(statusStr);
        String qq = normalize(q);

        if (userId != null && userId <= 0) {
            throw new BadRequestException(
                    "Invalid userId",
                    ErrorCode.BAD_REQUEST,
                    ErrorContext.of("userId", userId)
            );
        }

        return orderRepository.adminSearch(status, qq, userId, pageable);
    }

    @Transactional(readOnly = true)
    public AdminOrderDetailDto getDetail(Long id) {
        requireOrderId(id);

        Order order = orderRepository.findAdminDetailById(id)
                .orElseThrow(() -> new NotFoundException(
                        "Order not found",
                        ErrorCode.ORDER_NOT_FOUND,
                        ErrorContext.of("orderId", id)
                ));

        orderItemRepository.fetchCustomizationsForOrder(order.getId());

        List<AdminOrderItemDto> items = order.getItems().stream()
                .map(this::mapItem)
                .toList();

        BigDecimal total = items.stream()
                .map(AdminOrderItemDto::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long itemCount = items.stream()
                .mapToLong(AdminOrderItemDto::quantity)
                .sum();

        List<AdminOrderStatusChangeDto> history = statusChangeRepository
                .findByOrderIdOrderByChangedAtAsc(order.getId())
                .stream()
                .map(sc -> new AdminOrderStatusChangeDto(sc.getStatus(), sc.getChangedAt()))
                .toList();

        String username = (order.getUser() != null) ? order.getUser().getUsername() : null;

        return new AdminOrderDetailDto(
                order.getId(),
                order.getStatus(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                username,
                order.getGuestToken(),
                order.getDeliveryPhone(),
                order.getDeliveryAddress(),
                total,
                itemCount,
                items,
                history
        );
    }

    @Transactional
    public AdminOrderStatusUpdateDto adminStartPreparing(Long orderId) {
        Order order = getOrderOrThrow(orderId);
        requireStatus(order, OrderStatus.ORDERED);
        changeStatus(order, OrderStatus.PREPARING);
        return new AdminOrderStatusUpdateDto(order.getId(), order.getStatus(), order.getUpdatedAt());
    }

    @Transactional
    public AdminOrderStatusUpdateDto adminOutForDelivery(Long orderId) {
        Order order = getOrderOrThrow(orderId);
        requireStatus(order, OrderStatus.PREPARING);
        changeStatus(order, OrderStatus.OUT_FOR_DELIVERY);
        return new AdminOrderStatusUpdateDto(order.getId(), order.getStatus(), order.getUpdatedAt());
    }

    @Transactional
    public AdminOrderStatusUpdateDto adminDeliver(Long orderId) {
        Order order = getOrderOrThrow(orderId);
        requireStatus(order, OrderStatus.OUT_FOR_DELIVERY);
        changeStatus(order, OrderStatus.DELIVERED);
        return new AdminOrderStatusUpdateDto(order.getId(), order.getStatus(), order.getUpdatedAt());
    }

    @Transactional
    public AdminOrderStatusUpdateDto adminCancel(Long orderId) {
        Order order = getOrderOrThrow(orderId);

        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new ConflictException(
                    "Cannot cancel a delivered order",
                    ErrorCode.ORDER_ALREADY_DELIVERED,
                    ErrorContext.of("orderId", orderId)
            );
        }
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new ConflictException(
                    "Order is already cancelled",
                    ErrorCode.ORDER_ALREADY_CANCELLED,
                    ErrorContext.of("orderId", orderId)
            );
        }
        if (order.getStatus() == OrderStatus.CART) {
            throw new BadRequestException(
                    "Cannot cancel a cart",
                    ErrorCode.ORDER_IS_CART,
                    ErrorContext.of("orderId", orderId)
            );
        }

        changeStatus(order, OrderStatus.CANCELLED);
        return new AdminOrderStatusUpdateDto(order.getId(), order.getStatus(), order.getUpdatedAt());
    }

    private AdminOrderItemDto mapItem(OrderItem oi) {
        List<AdminOrderItemCustomizationDto> customizations =
                (oi.getCustomizations() == null ? List.<OrderItemCustomization>of() : oi.getCustomizations())
                        .stream()
                        .sorted(Comparator.comparing(c -> c.getId() == null ? Long.MAX_VALUE : c.getId()))
                        .map(c -> new AdminOrderItemCustomizationDto(
                                c.getAction() != null ? c.getAction().name() : null,
                                (c.getIngredient() != null) ? c.getIngredient().getName() : null
                        ))
                        .toList();

        BigDecimal unitPrice = (oi.getUnitPrice() == null) ? BigDecimal.ZERO : oi.getUnitPrice();
        BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(oi.getQuantity()));

        String name = null;
        ProductType type = null;
        String imageUrl = null;

        if (oi.getProduct() != null) {
            name = oi.getProduct().getName();
            type = oi.getProduct().getType();
            imageUrl = oi.getProduct().getImageUrl();
        }

        String variantLabel = (oi.getPizzaVariant() != null)
                ? oi.getPizzaVariant().getDough().name() + " " + oi.getPizzaVariant().getSize().name()
                : null;

        return new AdminOrderItemDto(
                oi.getId(),
                name,
                type,
                imageUrl,
                variantLabel,
                oi.getQuantity(),
                unitPrice,
                lineTotal,
                oi.getNote(),
                customizations
        );
    }

    private static String normalize(String q) {
        if (q == null) return null;
        String t = q.trim();
        return t.isEmpty() ? null : t;
    }

    private OrderStatus parseStatusOrThrow(String statusStr) {
        if (statusStr == null) return null;

        String s = statusStr.trim();
        if (s.isEmpty() || s.equalsIgnoreCase("all")) return null;

        try {
            return OrderStatus.valueOf(s.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException(
                    "Invalid status filter",
                    ErrorCode.INVALID_STATUS_FILTER,
                    ErrorContext.of("status", statusStr)
            );
        }
    }

    private void requireOrderId(Long orderId) {
        if (orderId == null || orderId <= 0) {
            throw new BadRequestException(
                    "Invalid order id",
                    ErrorCode.INVALID_ORDER_ID,
                    ErrorContext.of("orderId", orderId)
            );
        }
    }

    private Order getOrderOrThrow(Long orderId) {
        requireOrderId(orderId);

        return orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException(
                        "Order not found",
                        ErrorCode.ORDER_NOT_FOUND,
                        ErrorContext.of("orderId", orderId)
                ));
    }

    private void requireStatus(Order order, OrderStatus expected) {
        if (order.getStatus() != expected) {
            throw new ConflictException(
                    "Invalid status transition",
                    ErrorCode.INVALID_STATUS_TRANSITION,
                    Map.of(
                            "orderId", order.getId(),
                            "expected", expected.name(),
                            "actual", order.getStatus() != null ? order.getStatus().name() : null
                    )
            );
        }
    }

    private void changeStatus(Order order, OrderStatus newStatus) {
        LocalDateTime now = LocalDateTime.now();

        order.setStatus(newStatus);
        order.setUpdatedAt(now);
        orderRepository.save(order);

        OrderStatusChange sc = new OrderStatusChange();
        sc.setOrder(order);
        sc.setStatus(newStatus);
        sc.setChangedAt(now);
        statusChangeRepository.save(sc);
    }
}
