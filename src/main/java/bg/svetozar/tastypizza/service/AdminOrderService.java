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

import static bg.svetozar.tastypizza.exception.ErrorMessage.INVALID_USER_ID;
import static bg.svetozar.tastypizza.exception.ErrorMessage.ORDER_NOT_FOUND;
import static bg.svetozar.tastypizza.exception.ErrorMessage.ORDER_ALREADY_DELIVERED;
import static bg.svetozar.tastypizza.exception.ErrorMessage.ORDER_ALREADY_CANCELLED;
import static bg.svetozar.tastypizza.exception.ErrorMessage.ORDER_IS_CART;
import static bg.svetozar.tastypizza.exception.ErrorMessage.INVALID_STATUS_FILTER;
import static bg.svetozar.tastypizza.exception.ErrorMessage.INVALID_STATUS_TRANSITION;
import static bg.svetozar.tastypizza.exception.ErrorMessage.INVALID_ORDER_ID;

@Service
@RequiredArgsConstructor
public class AdminOrderService {

    private final OrderRepository orderRepository;
    private final OrderStatusChangeRepository statusChangeRepository;
    private final OrderItemRepository orderItemRepository;

    @Transactional(readOnly = true)
    public Page<AdminOrderListDto> list(String statusStr, String query, Long userId, Pageable pageable) {
        OrderStatus status = parseStatusOrThrow(statusStr);
        String normalizedQuery = normalize(query);

        if (userId != null && userId <= 0) {
            throw new BadRequestException(
                    INVALID_USER_ID,
                    ErrorCode.BAD_REQUEST,
                    ErrorContext.of("userId", userId)
            );
        }

        return orderRepository.adminSearch(status, normalizedQuery, userId, pageable);
    }

    @Transactional(readOnly = true)
    public AdminOrderDetailDto getDetail(Long id) {
        requireOrderId(id);

        Order order = orderRepository.findAdminDetailById(id)
                .orElseThrow(() -> new NotFoundException(
                        ORDER_NOT_FOUND,
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
                .map(orderStatusChange ->
                        new AdminOrderStatusChangeDto(
                                orderStatusChange.getStatus(),
                                orderStatusChange.getChangedAt()))
                .toList();

        String username = (order.getUser() != null)
                ? order.getUser().getUsername()
                : null;

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
                    ORDER_ALREADY_DELIVERED,
                    ErrorCode.ORDER_ALREADY_DELIVERED,
                    ErrorContext.of("orderId", orderId)
            );
        }

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new ConflictException(
                    ORDER_ALREADY_CANCELLED,
                    ErrorCode.ORDER_ALREADY_CANCELLED,
                    ErrorContext.of("orderId", orderId)
            );
        }

        if (order.getStatus() == OrderStatus.CART) {
            throw new BadRequestException(
                    ORDER_IS_CART,
                    ErrorCode.ORDER_IS_CART,
                    ErrorContext.of("orderId", orderId)
            );
        }

        changeStatus(order, OrderStatus.CANCELLED);
        return new AdminOrderStatusUpdateDto(order.getId(), order.getStatus(), order.getUpdatedAt());
    }

    private AdminOrderItemDto mapItem(OrderItem orderItem) {
        List<AdminOrderItemCustomizationDto> customizations =
                (orderItem.getCustomizations() == null
                        ? List.<OrderItemCustomization>of()
                        : orderItem.getCustomizations())
                        .stream()
                        .sorted(Comparator.comparing(orderItemCustomization ->
                                orderItemCustomization.getId() == null ? Long.MAX_VALUE : orderItemCustomization.getId()))
                        .map(orderItemCustomization -> new AdminOrderItemCustomizationDto(
                                orderItemCustomization.getAction() != null ? orderItemCustomization.getAction().name() : null,
                                orderItemCustomization.getIngredient() != null ? orderItemCustomization.getIngredient().getName() : null
                        ))
                        .toList();

        BigDecimal unitPrice = orderItem.getUnitPrice() != null
                ? orderItem.getUnitPrice()
                : BigDecimal.ZERO;

        BigDecimal lineTotal = unitPrice.multiply(
                BigDecimal.valueOf(orderItem.getQuantity())
        );

        String name = null;
        ProductType type = null;
        String imageUrl = null;

        if (orderItem.getProduct() != null) {
            name = orderItem.getProduct().getName();
            type = orderItem.getProduct().getType();
            imageUrl = orderItem.getProduct().getImageUrl();
        }

        String variantLabel = orderItem.getPizzaVariant() != null
                ? orderItem.getPizzaVariant().getDough().name() + " "
                + orderItem.getPizzaVariant().getSize().name()
                : null;

        return new AdminOrderItemDto(
                orderItem.getId(),
                name,
                type,
                imageUrl,
                variantLabel,
                orderItem.getQuantity(),
                unitPrice,
                lineTotal,
                orderItem.getNote(),
                customizations
        );
    }

    private static String normalize(String query) {
        if (query == null) return null;
        String trimmed = query.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private OrderStatus parseStatusOrThrow(String statusStr) {
        if (statusStr == null) return null;

        String s = statusStr.trim();
        if (s.isEmpty() || s.equalsIgnoreCase("all")) return null;

        try {
            return OrderStatus.valueOf(s.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException(
                    INVALID_STATUS_FILTER,
                    ErrorCode.INVALID_STATUS_FILTER,
                    ErrorContext.of("status", statusStr)
            );
        }
    }

    private void requireOrderId(Long orderId) {
        if (orderId == null || orderId <= 0) {
            throw new BadRequestException(
                    INVALID_ORDER_ID,
                    ErrorCode.INVALID_ORDER_ID,
                    ErrorContext.of("orderId", orderId)
            );
        }
    }

    private Order getOrderOrThrow(Long orderId) {
        requireOrderId(orderId);

        return orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException(
                        ORDER_NOT_FOUND,
                        ErrorCode.ORDER_NOT_FOUND,
                        ErrorContext.of("orderId", orderId)
                ));
    }

    private void requireStatus(Order order, OrderStatus expected) {
        if (order.getStatus() != expected) {
            throw new ConflictException(
                    INVALID_STATUS_TRANSITION,
                    ErrorCode.INVALID_STATUS_TRANSITION,
                    Map.of(
                            "orderId", order.getId(),
                            "expected", expected.name(),
                            "actual", order.getStatus() != null
                                    ? order.getStatus().name()
                                    : null
                    )
            );
        }
    }

    private void changeStatus(Order order, OrderStatus newStatus) {
        LocalDateTime now = LocalDateTime.now();

        order.setStatus(newStatus);
        order.setUpdatedAt(now);
        orderRepository.save(order);

        OrderStatusChange change = new OrderStatusChange();
        change.setOrder(order);
        change.setStatus(newStatus);
        change.setChangedAt(now);
        statusChangeRepository.save(change);
    }
}
