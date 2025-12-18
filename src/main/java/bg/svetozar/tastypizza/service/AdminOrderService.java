package bg.svetozar.tastypizza.service;

import bg.svetozar.tastypizza.model.dto.admin.*;
import bg.svetozar.tastypizza.model.entity.Order;
import bg.svetozar.tastypizza.model.entity.OrderItem;
import bg.svetozar.tastypizza.model.entity.OrderItemCustomization;
import bg.svetozar.tastypizza.model.entity.OrderStatusChange;
import bg.svetozar.tastypizza.model.enums.OrderStatus;
import bg.svetozar.tastypizza.model.enums.ProductType;
import bg.svetozar.tastypizza.repository.OrderRepository;
import bg.svetozar.tastypizza.repository.OrderStatusChangeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminOrderService {

    private final OrderRepository orderRepository;
    private final OrderStatusChangeRepository statusChangeRepository;


    @Transactional(readOnly = true)
    public Page<AdminOrderListDto> list(String statusStr, String q, Pageable pageable) {
        OrderStatus status = parseStatusOrNull(statusStr);
        String qq = normalize(q);
        return orderRepository.adminSearch(status, qq, pageable);
    }

    // -------- DETAIL (single order with items + history) --------

    @Transactional(readOnly = true)
    public AdminOrderDetailDto getDetail(Long id) {
        Order order = orderRepository.findAdminDetailById(id)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));

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

        String username = order.getUser() != null ? order.getUser().getUsername() : null;

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

        // allow cancel from these states only
        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new IllegalStateException("Cannot cancel a delivered order");
        }
        if (order.getStatus() == OrderStatus.CANCELLED) {
            // може и да върнем ok без промяна, но по-чисто е грешка
            throw new IllegalStateException("Order is already cancelled");
        }
        if (order.getStatus() == OrderStatus.CART) {
            throw new IllegalStateException("Cannot cancel a cart");
        }

        changeStatus(order, OrderStatus.CANCELLED);
        return new AdminOrderStatusUpdateDto(order.getId(), order.getStatus(), order.getUpdatedAt());
    }


    // -------- MAPPERS --------

    private AdminOrderItemDto mapItem(OrderItem oi) {
        List<AdminOrderItemCustomizationDto> customizations =
                (oi.getCustomizations() == null ? List.<OrderItemCustomization>of() : oi.getCustomizations())
                        .stream()
                        .sorted(Comparator.comparing(c -> c.getId() == null ? Long.MAX_VALUE : c.getId()))
                        .map(c -> new AdminOrderItemCustomizationDto(
                                c.getAction().name(),
                                c.getIngredient().getName()
                        ))
                        .toList();

        BigDecimal unitPrice = oi.getUnitPrice() == null ? BigDecimal.ZERO : oi.getUnitPrice();
        BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(oi.getQuantity()));

        String name = null;
        ProductType type = null;
        String imageUrl = null;

        if (oi.getProduct() != null) {
            name = oi.getProduct().getName();
            type = oi.getProduct().getType() != null ? oi.getProduct().getType() : null;
            imageUrl = oi.getProduct().getImageUrl();
        }

        String variantLabel = (oi.getPizzaVariant() != null) ? oi.getPizzaVariant().getDough().name() + " " + oi.getPizzaVariant().getSize().name() : null;

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

    // -------- HELPERS --------

    private static String normalize(String q) {
        if (q == null) return null;
        String t = q.trim();
        return t.isEmpty() ? "" : t;
    }

    private static OrderStatus parseStatusOrNull(String statusStr) {
        if (statusStr == null) return null;
        String s = statusStr.trim();
        if (s.isEmpty() || s.equalsIgnoreCase("all")) return null;
        return OrderStatus.valueOf(s.toUpperCase());
    }
    private Order getOrderOrThrow(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));
    }

    private void requireStatus(Order order, OrderStatus expected) {
        if (order.getStatus() != expected) {
            throw new IllegalStateException(
                    "Invalid status transition. Expected " + expected + " but was " + order.getStatus()
            );
        }
    }

    private void changeStatus(Order order, OrderStatus newStatus) {
        order.setStatus(newStatus);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);

        OrderStatusChange sc = new OrderStatusChange();
        sc.setOrder(order);
        sc.setStatus(newStatus);
        sc.setChangedAt(LocalDateTime.now());
        statusChangeRepository.save(sc);
    }
}
