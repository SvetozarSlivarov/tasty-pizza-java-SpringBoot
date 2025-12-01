package bg.svetozar.tastypizza.service;

import bg.svetozar.tastypizza.model.dto.order.CartDto;
import bg.svetozar.tastypizza.model.entity.*;
import bg.svetozar.tastypizza.model.enums.OrderStatus;
import bg.svetozar.tastypizza.model.enums.ProductType;
import bg.svetozar.tastypizza.model.mapper.OrderMapper;
import bg.svetozar.tastypizza.repository.OrderItemRepository;
import bg.svetozar.tastypizza.repository.OrderRepository;
import bg.svetozar.tastypizza.repository.PizzaVariantRepository;
import bg.svetozar.tastypizza.repository.ProductRepository;
import bg.svetozar.tastypizza.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final PizzaVariantRepository pizzaVariantRepository;

    private User getCurrentUserOrThrow() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("User must be authenticated to use cart");
        }

        String username = auth.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found: " + username));
    }

    private Order getOrCreateCart(User user) {
        return orderRepository.findFirstByUserAndStatusOrderByIdDesc(user, OrderStatus.CART)
                .orElseGet(() -> {
                    Order order = Order.builder()
                            .user(user)
                            .status(OrderStatus.CART)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build();
                    return orderRepository.save(order);
                });
    }

    public CartDto getCurrentCart() {
        User user = getCurrentUserOrThrow();
        Order cart = getOrCreateCart(user);
        return OrderMapper.toCartDto(cart);
    }

    public CartDto addDrinkToCart(Long drinkProductId, int quantity, String note) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        User user = getCurrentUserOrThrow();
        Order cart = getOrCreateCart(user);

        Product product = productRepository.findById(drinkProductId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + drinkProductId));

        if (product.getType() != ProductType.DRINK) {
            throw new IllegalArgumentException("Product " + drinkProductId + " is not a drink");
        }

        BigDecimal unitPrice = product.getBasePrice();

        OrderItem item = OrderItem.builder()
                .order(cart)
                .product(product)
                .quantity(quantity)
                .unitPrice(unitPrice)
                .note(note)
                .build();

        orderItemRepository.save(item);
        cart.getItems().add(item);
        cart.setUpdatedAt(LocalDateTime.now());

        return OrderMapper.toCartDto(cart);
    }

    public CartDto addPizzaToCart(Long pizzaProductId,
                                  Long variantId,
                                  int quantity,
                                  String note) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        User user = getCurrentUserOrThrow();
        Order cart = getOrCreateCart(user);

        Product product = productRepository.findById(pizzaProductId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + pizzaProductId));

        if (product.getType() != ProductType.PIZZA) {
            throw new IllegalArgumentException("Product " + pizzaProductId + " is not a pizza");
        }

        PizzaVariant variant = pizzaVariantRepository.findById(variantId)
                .orElseThrow(() -> new IllegalArgumentException("Pizza variant not found: " + variantId));

        if (!variant.getPizza().getProduct().getId().equals(pizzaProductId)) {
            throw new IllegalArgumentException("Variant does not belong to given pizza");
        }

        BigDecimal unitPrice = product.getBasePrice()
                .add(variant.getExtraPrice());

        OrderItem item = OrderItem.builder()
                .order(cart)
                .product(product)
                .pizzaVariant(variant)
                .quantity(quantity)
                .unitPrice(unitPrice)
                .note(note)
                .build();

        orderItemRepository.save(item);
        cart.getItems().add(item);
        cart.setUpdatedAt(LocalDateTime.now());

        return OrderMapper.toCartDto(cart);
    }

    public CartDto updateQuantity(Long orderItemId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        User user = getCurrentUserOrThrow();

        OrderItem item = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new IllegalArgumentException("Order item not found: " + orderItemId));

        if (!item.getOrder().getUser().getId().equals(user.getId())) {
            throw new IllegalStateException("Cannot modify item from different user");
        }

        item.setQuantity(quantity);
        item.getOrder().setUpdatedAt(LocalDateTime.now());

        return OrderMapper.toCartDto(item.getOrder());
    }

    public CartDto removeItem(Long orderItemId) {
        User user = getCurrentUserOrThrow();

        OrderItem item = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new IllegalArgumentException("Order item not found: " + orderItemId));

        Order order = item.getOrder();

        if (!order.getUser().getId().equals(user.getId())) {
            throw new IllegalStateException("Cannot delete item from different user");
        }

        order.getItems().remove(item);
        orderItemRepository.delete(item);

        order.setUpdatedAt(LocalDateTime.now());

        return OrderMapper.toCartDto(order);
    }

    public CartDto checkout(String phone, String address) {
        User user = getCurrentUserOrThrow();
        Order cart = getOrCreateCart(user);

        if (cart.getItems().isEmpty()) {
            throw new IllegalStateException("Cannot checkout empty cart");
        }

        cart.setDeliveryPhone(phone);
        cart.setDeliveryAddress(address);
        cart.setStatus(OrderStatus.ORDERED);
        cart.setUpdatedAt(LocalDateTime.now());

        OrderStatusChange change = OrderStatusChange.builder()
                .order(cart)
                .status(OrderStatus.ORDERED)
                .changedAt(LocalDateTime.now())
                .build();

        cart.getStatusChanges().add(change);

        return OrderMapper.toCartDto(cart);
    }
}
