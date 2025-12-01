package bg.svetozar.tastypizza.service;

import bg.svetozar.tastypizza.model.dto.order.CartDto;
import bg.svetozar.tastypizza.model.entity.*;
import bg.svetozar.tastypizza.model.enums.OrderItemCustomizationAction;
import bg.svetozar.tastypizza.model.enums.OrderStatus;
import bg.svetozar.tastypizza.model.enums.ProductType;
import bg.svetozar.tastypizza.model.mapper.OrderMapper;
import bg.svetozar.tastypizza.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final PizzaVariantRepository pizzaVariantRepository;
    private final OrderItemCustomizationRepository orderItemCustomizationRepository;
    private final IngredientRepository ingredientRepository;
    private final PizzaRepository pizzaRepository;
    private final PizzaIngredientRepository pizzaIngredientRepository;
    private final PizzaAllowedIngredientRepository pizzaAllowedIngredientRepository;

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
    private void applyPizzaCustomizations(OrderItem item,
                                          Pizza pizza,
                                          List<Long> removeIngredientIds,
                                          List<Long> addIngredientIds) {

        removeIngredientIds = removeIngredientIds != null ? removeIngredientIds : List.of();
        addIngredientIds = addIngredientIds != null ? addIngredientIds : List.of();

        var removeSet = new java.util.HashSet<>(removeIngredientIds);
        var addSet = new java.util.HashSet<>(addIngredientIds);

        for (Long id : removeIngredientIds) {
            if (addSet.contains(id)) {
                throw new IllegalArgumentException("Ingredient " + id + " cannot be both added and removed");
            }
        }

        var allIds = new java.util.HashSet<Long>();
        allIds.addAll(removeSet);
        allIds.addAll(addSet);

        if (!allIds.isEmpty()) {
            var ingredients = ingredientRepository.findAllById(allIds);
            if (ingredients.size() != allIds.size()) {
                throw new IllegalArgumentException("Some ingredients do not exist");
            }
        }

        var baseByIngredientId = pizza.getIngredients()
                .stream()
                .collect(java.util.stream.Collectors.toMap(
                        pi -> pi.getIngredient().getId(),
                        pi -> pi
                ));

        var allowedByIngredientId = pizza.getAllowedIngredients()
                .stream()
                .collect(java.util.stream.Collectors.toMap(
                        pai -> pai.getIngredient().getId(),
                        pai -> pai
                ));

        for (Long ingId : removeSet) {
            var base = baseByIngredientId.get(ingId);
            if (base == null) {
                throw new IllegalArgumentException("Ingredient " + ingId + " is not in base recipe");
            }
            if (!base.isRemovable()) {
                throw new IllegalArgumentException("Ingredient " + ingId + " cannot be removed");
            }

            Ingredient ingredient = base.getIngredient();

            OrderItemCustomization customization = OrderItemCustomization.builder()
                    .orderItem(item)
                    .ingredient(ingredient)
                    .action(OrderItemCustomizationAction.REMOVE)
                    .build();

            item.getCustomizations().add(customization);
        }

        for (Long ingId : addSet) {
            var allowed = allowedByIngredientId.get(ingId);
            if (allowed == null) {
                throw new IllegalArgumentException("Ingredient " + ingId + " is not allowed for this pizza");
            }

            Ingredient ingredient = allowed.getIngredient();

            OrderItemCustomization customization = OrderItemCustomization.builder()
                    .orderItem(item)
                    .ingredient(ingredient)
                    .action(OrderItemCustomizationAction.ADD)
                    .build();

            item.getCustomizations().add(customization);
        }
        orderItemCustomizationRepository.saveAll(item.getCustomizations());
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
                                  String note,
                                  List<Long> removeIngredientIds,
                                  List<Long> addIngredientIds) {
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

        Pizza pizza = pizzaRepository.findByProduct(product)
                .orElseThrow(() -> new IllegalArgumentException("Pizza entity not found for product " + pizzaProductId));

        PizzaVariant variant = pizzaVariantRepository.findById(variantId)
                .orElseThrow(() -> new IllegalArgumentException("Pizza variant not found: " + variantId));

        if (!variant.getPizza().getId().equals(pizza.getId())) {
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

        applyPizzaCustomizations(item, pizza, removeIngredientIds, addIngredientIds);

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
    public CartDto updatePizzaCustomizations(Long orderItemId,
                                             List<Long> removeIngredientIds,
                                             List<Long> addIngredientIds) {
        User user = getCurrentUserOrThrow();

        OrderItem item = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new IllegalArgumentException("Order item not found: " + orderItemId));

        Order order = item.getOrder();

        if (!order.getUser().getId().equals(user.getId())) {
            throw new IllegalStateException("Cannot modify item from different user");
        }

        if (item.getProduct().getType() != ProductType.PIZZA) {
            throw new IllegalStateException("Only pizza items support customizations");
        }

        Pizza pizza = pizzaRepository.findByProduct(item.getProduct())
                .orElseThrow(() -> new IllegalArgumentException("Pizza entity not found for product " + item.getProduct().getId()));

        item.getCustomizations().clear();
        orderItemCustomizationRepository.deleteAll(
                orderItemCustomizationRepository.findByOrderItem(item)
        );

        applyPizzaCustomizations(item, pizza, removeIngredientIds, addIngredientIds);

        order.setUpdatedAt(LocalDateTime.now());

        return OrderMapper.toCartDto(order);
    }
    public CartDto updateNote(Long orderItemId, String note) {
        User user = getCurrentUserOrThrow();

        OrderItem item = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new IllegalArgumentException("Order item not found: " + orderItemId));

        if (!item.getOrder().getUser().getId().equals(user.getId())) {
            throw new IllegalStateException("Cannot modify item from different user");
        }

        item.setNote(note);
        item.getOrder().setUpdatedAt(LocalDateTime.now());

        return OrderMapper.toCartDto(item.getOrder());
    }

    public CartDto updateVariant(Long orderItemId, Long variantId) {
        User user = getCurrentUserOrThrow();

        OrderItem item = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new IllegalArgumentException("Order item not found: " + orderItemId));

        Order order = item.getOrder();

        if (!order.getUser().getId().equals(user.getId())) {
            throw new IllegalStateException("Cannot modify item from different user");
        }

        if (item.getProduct().getType() != ProductType.PIZZA) {
            throw new IllegalStateException("Only pizza items can change variant");
        }

        PizzaVariant variant = pizzaVariantRepository.findById(variantId)
                .orElseThrow(() -> new IllegalArgumentException("Pizza variant not found: " + variantId));

        if (!variant.getPizza().getProduct().getId().equals(item.getProduct().getId())) {
            throw new IllegalArgumentException("Variant does not belong to this pizza");
        }

        BigDecimal unitPrice = item.getProduct().getBasePrice()
                .add(variant.getExtraPrice());

        item.setPizzaVariant(variant);
        item.setUnitPrice(unitPrice);

        order.setUpdatedAt(LocalDateTime.now());

        return OrderMapper.toCartDto(order);
    }

}
