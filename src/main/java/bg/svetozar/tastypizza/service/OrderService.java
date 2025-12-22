package bg.svetozar.tastypizza.service;

import bg.svetozar.tastypizza.exception.*;
import bg.svetozar.tastypizza.model.dto.order.CartDto;
import bg.svetozar.tastypizza.model.dto.order.OrderStatusChangeDTO;
import bg.svetozar.tastypizza.model.dto.order.ReorderResultDto;
import bg.svetozar.tastypizza.model.entity.Order;
import bg.svetozar.tastypizza.model.entity.OrderItem;
import bg.svetozar.tastypizza.model.entity.OrderItemCustomization;
import bg.svetozar.tastypizza.model.entity.User;
import bg.svetozar.tastypizza.model.enums.OrderItemCustomizationAction;
import bg.svetozar.tastypizza.model.enums.OrderStatus;
import bg.svetozar.tastypizza.model.enums.ProductType;
import bg.svetozar.tastypizza.model.enums.UserRole;
import bg.svetozar.tastypizza.model.mapper.OrderMapper;
import bg.svetozar.tastypizza.repository.OrderRepository;
import bg.svetozar.tastypizza.repository.OrderStatusChangeRepository;
import bg.svetozar.tastypizza.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final OrderStatusChangeRepository statusChangeRepository;
    private final CartService cartService;

    private User getCurrentUserOrThrow() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new UnauthorizedException("Authentication required");
        }

        String username = auth.getName();

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UnauthorizedException("Invalid authentication principal"));
    }

    private Order requireOrder(Long orderId) {
        if (orderId == null || orderId <= 0) {
            throw new BadRequestException(
                    "Invalid order id",
                    ErrorCode.BAD_REQUEST,
                    ErrorContext.of("orderId", orderId)
            );
        }

        return orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException(
                        "Order not found",
                        ErrorCode.NOT_FOUND,
                        ErrorContext.of("orderId", orderId)
                ));
    }

    private void ensureCanAccessOrder(User requester, Order order) {
        if (requester.getRole() == UserRole.ADMIN) return;

        if (order.getUser() == null || !order.getUser().getId().equals(requester.getId())) {
            throw new ForbiddenException(
                    "Not allowed to access this order",
                    ErrorCode.FORBIDDEN,
                    ErrorContext.of("orderId", order.getId())
            );
        }
    }

    @Transactional(readOnly = true)
    public List<CartDto> getMyOrders() {
        User user = getCurrentUserOrThrow();
        return orderRepository.findMyOrdersWithItems(user)
                .stream()
                .map(OrderMapper::toCartDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrderStatusChangeDTO> getStatusHistory(Long orderId) {

        User requester = getCurrentUserOrThrow();

        Order order = requireOrder(orderId);

        ensureCanAccessOrder(requester, order);

        return statusChangeRepository
                .findByOrderIdOrderByChangedAtAsc(orderId)
                .stream()
                .map(sc -> new OrderStatusChangeDTO(sc.getStatus(), sc.getChangedAt()))
                .toList();
    }

    @Transactional
    public ReorderResultDto reorderIntoCart(Long sourceOrderId, String guestToken) {
        User user = getCurrentUserOrThrow();

        Order source = requireOrder(sourceOrderId);
        ensureCanAccessOrder(user, source);

        if (source.getStatus() == OrderStatus.CART) {
            throw new BadRequestException(
                    "Cannot reorder a cart",
                    ErrorCode.BAD_REQUEST,
                    ErrorContext.of("orderId", sourceOrderId)
            );
        }

        int added = 0;
        int skipped = 0;
        List<String> messages = new ArrayList<>();

        Order cartOrder = cartService.getCurrentCartEntity(guestToken, user);
        CartDto cart = OrderMapper.toCartDto(cartOrder);

        for (OrderItem item : source.getItems()) {
            try {
                if (item.getProduct() == null) {
                    skipped++;
                    messages.add("Item skipped: missing product");
                    continue;
                }

                ProductType type = item.getProduct().getType();
                if (type == null) {
                    skipped++;
                    messages.add("Item skipped: missing product type for productId=" + item.getProduct().getId());
                    continue;
                }

                if (type == ProductType.DRINK) {
                    cart = cartService.addDrinkToExistingCart(
                            cartOrder,
                            item.getProduct().getId(),
                            item.getQuantity(),
                            item.getNote()
                    );
                    added++;
                    continue;
                }

                if (type == ProductType.PIZZA) {
                    Long variantId = item.getPizzaVariant() != null ? item.getPizzaVariant().getId() : null;
                    if (variantId == null) {
                        skipped++;
                        messages.add("Pizza skipped: missing variant for productId=" + item.getProduct().getId());
                        continue;
                    }

                    List<Long> removeIds = new ArrayList<>();
                    List<Long> addIds = new ArrayList<>();

                    if (item.getCustomizations() != null) {
                        for (OrderItemCustomization c : item.getCustomizations()) {
                            if (c.getIngredient() == null || c.getAction() == null) continue;
                            if (c.getAction() == OrderItemCustomizationAction.REMOVE) removeIds.add(c.getIngredient().getId());
                            if (c.getAction() == OrderItemCustomizationAction.ADD) addIds.add(c.getIngredient().getId());
                        }
                    }

                    cart = cartService.addPizzaToExistingCart(
                            cartOrder,
                            item.getProduct().getId(),
                            variantId,
                            item.getQuantity(),
                            item.getNote(),
                            removeIds,
                            addIds
                    );
                    added++;
                    continue;
                }

                skipped++;
                messages.add("Item skipped: unsupported product type for productId=" + item.getProduct().getId());

            } catch (Exception ex) {
                skipped++;
                String name = item.getProduct() != null ? item.getProduct().getName() : "(unknown product)";
                String msg = ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName();
                messages.add("Skipped '" + name + "': " + msg);
            }
        }

        return new ReorderResultDto(cart, added, skipped, messages);
    }
}
