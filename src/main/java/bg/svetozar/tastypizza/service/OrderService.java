package bg.svetozar.tastypizza.service;

import bg.svetozar.tastypizza.model.dto.order.CartDto;
import bg.svetozar.tastypizza.model.dto.order.OrderStatusChangeDTO;
import bg.svetozar.tastypizza.model.dto.order.ReorderResultDto;
import bg.svetozar.tastypizza.model.entity.Order;
import bg.svetozar.tastypizza.model.entity.OrderItem;
import bg.svetozar.tastypizza.model.entity.OrderItemCustomization;
import bg.svetozar.tastypizza.model.entity.User;
import bg.svetozar.tastypizza.model.enums.OrderStatus;
import bg.svetozar.tastypizza.model.enums.OrderItemCustomizationAction;
import bg.svetozar.tastypizza.model.enums.ProductType;
import bg.svetozar.tastypizza.model.mapper.OrderMapper;
import bg.svetozar.tastypizza.repository.OrderItemRepository;
import bg.svetozar.tastypizza.repository.OrderRepository;
import bg.svetozar.tastypizza.repository.OrderStatusChangeRepository;
import bg.svetozar.tastypizza.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
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
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("User must be authenticated");
        }

        String username = auth.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found: " + username));
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
    public List<OrderStatusChangeDTO> getStatusHistory(Long orderId, String userEmail) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));

        return statusChangeRepository
                .findByOrderIdOrderByChangedAtAsc(orderId)
                .stream()
                .map(sc -> new OrderStatusChangeDTO(
                        sc.getStatus(),
                        sc.getChangedAt()
                ))
                .toList();
    }
    @Transactional
    public ReorderResultDto reorderIntoCart(Long sourceOrderId, String guestToken) {
        User user = getCurrentUserOrThrow();

        Order source = orderRepository.findById(sourceOrderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));

        if (source.getUser() == null || !source.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("Not allowed to reorder this order");
        }

        if (source.getStatus() == OrderStatus.CART) {
            throw new IllegalArgumentException("Cannot reorder a cart");
        }

        int added = 0;
        int skipped = 0;
        List<String> messages = new ArrayList<>();

        CartDto cart = cartService.getCurrentCart(guestToken);

        for (OrderItem item : source.getItems()) {
            try {
                if (item.getProduct() == null) {
                    skipped++;
                    messages.add("Item skipped: missing product");
                    continue;
                }

                if (item.getProduct().getType() == ProductType.DRINK) {
                    cart = cartService.addDrinkToCart(
                            guestToken,
                            item.getProduct().getId(),
                            item.getQuantity(),
                            item.getNote()
                    );
                    added++;
                    continue;
                }

                if (item.getProduct().getType() == ProductType.PIZZA) {
                    Long variantId = item.getPizzaVariant() != null ? item.getPizzaVariant().getId() : null;
                    if (variantId == null) {
                        skipped++;
                        messages.add("Pizza skipped: missing variant for productId=" + item.getProduct().getId());
                        continue;
                    }

                    List<Long> removeIds = new ArrayList<>();
                    List<Long> addIds = new ArrayList<>();

                    for (OrderItemCustomization c : item.getCustomizations()) {
                        if (c.getIngredient() == null) continue;
                        if (c.getAction() == OrderItemCustomizationAction.REMOVE) removeIds.add(c.getIngredient().getId());
                        if (c.getAction() == OrderItemCustomizationAction.ADD) addIds.add(c.getIngredient().getId());
                    }

                    cart = cartService.addPizzaToCart(
                            guestToken,
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
                messages.add("Skipped '" + name + "': " + ex.getMessage());
            }
        }

        return new ReorderResultDto(cart, added, skipped, messages);
    }
}
