package bg.svetozar.tastypizza.service;

import bg.svetozar.tastypizza.exception.*;
import bg.svetozar.tastypizza.model.dto.order.CartDto;
import bg.svetozar.tastypizza.model.dto.order.UpdateCartItemRequest;
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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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

    private User getCurrentUserOrNull() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        String username = auth.getName();
        return userRepository.findByUsernameAndDeletedFalse(username).orElse(null);
    }

    private Order getOrCreateUserCart(User user) {
        return orderRepository.findFirstByUserAndStatusOrderByIdDesc(user, OrderStatus.CART)
                .orElseGet(() -> {
                    LocalDateTime now = LocalDateTime.now();
                    Order order = Order.builder()
                            .user(user)
                            .status(OrderStatus.CART)
                            .createdAt(now)
                            .updatedAt(now)
                            .build();
                    return orderRepository.save(order);
                });
    }

    private Order getOrCreateGuestCart(String guestToken) {
        requireGuestToken(guestToken);
        return orderRepository.findFirstByGuestTokenAndStatusOrderByIdDesc(guestToken, OrderStatus.CART)
                .orElseGet(() -> createGuestCart(guestToken));
    }

    private Order createGuestCart(String guestToken) {
        requireGuestToken(guestToken);

        LocalDateTime now = LocalDateTime.now();
        Order order = Order.builder()
                .user(null)
                .guestToken(guestToken)
                .status(OrderStatus.CART)
                .createdAt(now)
                .updatedAt(now)
                .build();

        return orderRepository.save(order);
    }

    public CartDto getCurrentCart(String guestToken) {
        User user = getCurrentUserOrNull();
        Order order = (user != null)
                ? mergeGuestCartIntoUserCart(user, guestToken)
                : getOrCreateGuestCart(guestToken);

        return OrderMapper.toCartDto(order);
    }

    public Order getCurrentCartEntity(String guestToken, User user) {
        if (user != null) return mergeGuestCartIntoUserCart(user, guestToken);
        return getOrCreateGuestCart(guestToken);
    }

    public CartDto addDrinkToCart(String guestToken, Long drinkProductId, int quantity, String note) {
        requirePositiveQty(quantity);

        User user = getCurrentUserOrNull();
        Order order = (user != null)
                ? mergeGuestCartIntoUserCart(user, guestToken)
                : getOrCreateGuestCart(guestToken);

        Product product = requireProduct(drinkProductId);
        ensureProductNotDeleted(product);
        ensureType(product, ProductType.DRINK);

        BigDecimal unitPrice = nz(product.getBasePrice());

        OrderItem item = OrderItem.builder()
                .order(order)
                .product(product)
                .quantity(quantity)
                .unitPrice(unitPrice)
                .note(note)
                .build();

        orderItemRepository.save(item);
        order.getItems().add(item);
        order.setUpdatedAt(LocalDateTime.now());

        return OrderMapper.toCartDto(order);
    }


    public CartDto addPizzaToCart(String guestToken,
                                  Long pizzaProductId,
                                  Long variantId,
                                  int quantity,
                                  String note,
                                  List<Long> removeIngredientIds,
                                  List<Long> addIngredientIds) {
        requirePositiveQty(quantity);
        requireId(variantId, "variantId", ErrorCode.VARIANT_REQUIRED, "Variant is required for pizza");

        User user = getCurrentUserOrNull();
        Order order = (user != null)
                ? mergeGuestCartIntoUserCart(user, guestToken)
                : getOrCreateGuestCart(guestToken);

        Product product = requireProduct(pizzaProductId);
        ensureProductNotDeleted(product);
        ensureType(product, ProductType.PIZZA);

        Pizza pizza = pizzaRepository.findByProduct(product)
                .orElseThrow(() -> new NotFoundException(
                        "Pizza entity not found for product",
                        ErrorCode.PIZZA_NOT_FOUND,
                        ErrorContext.of("productId", product.getId())
                ));

        PizzaVariant variant = pizzaVariantRepository.findById(variantId)
                .orElseThrow(() -> new NotFoundException(
                        "Pizza variant not found",
                        ErrorCode.VARIANT_NOT_FOUND,
                        ErrorContext.of("variantId", variantId)
                ));

        if (!variant.getPizza().getId().equals(pizza.getId())) {
            throw new BadRequestException(
                    "Variant does not belong to given pizza",
                    ErrorCode.VARIANT_NOT_BELONG_TO_PIZZA,
                    ctx("variantId", variantId, "pizzaId", pizza.getId())
            );
        }

        BigDecimal base = nz(product.getBasePrice());
        BigDecimal variantExtra = nz(variant.getExtraPrice());
        BigDecimal unitPrice = base.add(variantExtra);

        OrderItem item = OrderItem.builder()
                .order(order)
                .product(product)
                .pizzaVariant(variant)
                .quantity(quantity)
                .unitPrice(unitPrice)
                .note(note)
                .build();

        orderItemRepository.save(item);

        BigDecimal extrasSum = applyPizzaCustomizations(item, pizza, removeIngredientIds, addIngredientIds);
        item.setUnitPrice(unitPrice.add(extrasSum));

        order.getItems().add(item);
        order.setUpdatedAt(LocalDateTime.now());

        return OrderMapper.toCartDto(order);
    }

    public CartDto patchCartItem(String guestToken, Long orderItemId, UpdateCartItemRequest request) {
        User user = getCurrentUserOrNull();

        OrderItem item = requireOrderItem(orderItemId);
        Order order = item.getOrder();
        ensureCanModifyOrder(order, user, guestToken);

        boolean changed = false;

        if (request.quantity() != null) {
            requirePositiveQty(request.quantity());
            item.setQuantity(request.quantity());
            changed = true;
        }

        if (request.note() != null) {
            item.setNote(request.note());
            changed = true;
        }

        if (request.variantId() != null) {
            applyVariantChange(item, request.variantId());
            changed = true;
        }

        if (request.removeIngredientIds() != null || request.addIngredientIds() != null) {
            applyCustomizationChange(item, request.removeIngredientIds(), request.addIngredientIds());
            changed = true;
        }

        if (changed) {
            order.setUpdatedAt(LocalDateTime.now());
        }

        return OrderMapper.toCartDto(order);
    }

    public CartDto removeItem(String guestToken, Long orderItemId) {
        User user = getCurrentUserOrNull();
        OrderItem item = requireOrderItem(orderItemId);

        Order order = item.getOrder();
        ensureCanModifyOrder(order, user, guestToken);

        order.getItems().remove(item);
        orderItemRepository.delete(item);

        order.setUpdatedAt(LocalDateTime.now());

        return OrderMapper.toCartDto(order);
    }

    public CartDto checkout(String guestToken, String phone, String address) {
        User user = getCurrentUserOrNull();
        Order cart;

        if (user != null) {
            cart = mergeGuestCartIntoUserCart(user, guestToken);
        } else {
            requireGuestToken(guestToken);
            cart = getOrCreateGuestCart(guestToken);
        }

        if (cart.getStatus() != OrderStatus.CART) {
            throw new BadRequestException(
                    "Order is not a cart",
                    ErrorCode.INVALID_OPERATION,
                    ErrorContext.of("orderId", cart.getId())
            );
        }

        if (!StringUtils.hasText(phone)) {
            throw new BadRequestException(
                    "Phone is required",
                    ErrorCode.BAD_REQUEST,
                    ErrorContext.of("field", "phone")
            );
        }
        if (!StringUtils.hasText(address)) {
            throw new BadRequestException(
                    "Address is required",
                    ErrorCode.BAD_REQUEST,
                    ErrorContext.of("field", "address")
            );
        }

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new ConflictException(
                    "Cannot checkout empty cart",
                    ErrorCode.CART_EMPTY
            );
        }

        LocalDateTime now = LocalDateTime.now();

        cart.setDeliveryPhone(phone);
        cart.setDeliveryAddress(address);
        cart.setStatus(OrderStatus.ORDERED);
        cart.setUpdatedAt(now);

        OrderStatusChange change = OrderStatusChange.builder()
                .order(cart)
                .status(OrderStatus.ORDERED)
                .changedAt(now)
                .build();

        cart.getStatusChanges().add(change);

        return OrderMapper.toCartDto(cart);
    }

    public CartDto addDrinkToExistingCart(Order order, Long drinkProductId, int quantity, String note) {
        requirePositiveQty(quantity);

        Product product = requireProduct(drinkProductId);
        ensureProductNotDeleted(product);
        ensureType(product, ProductType.DRINK);

        BigDecimal unitPrice = nz(product.getBasePrice());

        OrderItem item = OrderItem.builder()
                .order(order)
                .product(product)
                .quantity(quantity)
                .unitPrice(unitPrice)
                .note(note)
                .build();

        orderItemRepository.save(item);
        order.getItems().add(item);
        order.setUpdatedAt(LocalDateTime.now());

        return OrderMapper.toCartDto(order);
    }

    public CartDto addPizzaToExistingCart(
            Order order,
            Long pizzaProductId,
            Long variantId,
            int quantity,
            String note,
            List<Long> removeIngredientIds,
            List<Long> addIngredientIds
    ) {
        requirePositiveQty(quantity);
        requireId(variantId, "variantId", ErrorCode.VARIANT_REQUIRED, "Variant is required for pizza");

        Product product = requireProduct(pizzaProductId);
        ensureProductNotDeleted(product);
        ensureType(product, ProductType.PIZZA);

        Pizza pizza = pizzaRepository.findByProduct(product)
                .orElseThrow(() -> new NotFoundException(
                        "Pizza entity not found for product",
                        ErrorCode.PIZZA_NOT_FOUND,
                        ErrorContext.of("productId", product.getId())
                ));

        PizzaVariant variant = pizzaVariantRepository.findById(variantId)
                .orElseThrow(() -> new NotFoundException(
                        "Pizza variant not found",
                        ErrorCode.VARIANT_NOT_FOUND,
                        ErrorContext.of("variantId", variantId)
                ));

        if (!variant.getPizza().getId().equals(pizza.getId())) {
            throw new BadRequestException(
                    "Variant does not belong to given pizza",
                    ErrorCode.VARIANT_NOT_BELONG_TO_PIZZA,
                    ctx("variantId", variantId, "pizzaId", pizza.getId())
            );
        }

        BigDecimal base = nz(product.getBasePrice());
        BigDecimal variantExtra = nz(variant.getExtraPrice());
        BigDecimal unitPrice = base.add(variantExtra);

        OrderItem item = OrderItem.builder()
                .order(order)
                .product(product)
                .pizzaVariant(variant)
                .quantity(quantity)
                .unitPrice(unitPrice)
                .note(note)
                .build();

        orderItemRepository.save(item);

        BigDecimal extrasSum = applyPizzaCustomizations(item, pizza, removeIngredientIds, addIngredientIds);
        item.setUnitPrice(unitPrice.add(extrasSum));

        order.getItems().add(item);
        order.setUpdatedAt(LocalDateTime.now());

        return OrderMapper.toCartDto(order);
    }

    private void applyVariantChange(OrderItem item, Long variantId) {
        if (item.getProduct().getType() != ProductType.PIZZA) {
            throw new BadRequestException(
                    "Only pizza items can change variant",
                    ErrorCode.INVALID_OPERATION,
                    ErrorContext.of("orderItemId", item.getId())
            );
        }

        requireId(variantId, "variantId", ErrorCode.VARIANT_REQUIRED, "Variant is required");

        PizzaVariant variant = pizzaVariantRepository.findById(variantId)
                .orElseThrow(() -> new NotFoundException(
                        "Pizza variant not found",
                        ErrorCode.VARIANT_NOT_FOUND,
                        ErrorContext.of("variantId", variantId)
                ));

        if (!variant.getPizza().getProduct().getId().equals(item.getProduct().getId())) {
            throw new BadRequestException(
                    "Variant does not belong to this pizza",
                    ErrorCode.VARIANT_NOT_BELONG_TO_PIZZA,
                    ctx("variantId", variantId, "productId", item.getProduct().getId())
            );
        }

        item.setPizzaVariant(variant);

        Pizza pizza = pizzaRepository.findByProduct(item.getProduct())
                .orElseThrow(() -> new NotFoundException(
                        "Pizza entity not found for product",
                        ErrorCode.PIZZA_NOT_FOUND,
                        ErrorContext.of("productId", item.getProduct().getId())
                ));

        recalcPizzaUnitPrice(item, pizza);
    }

    private void applyCustomizationChange(OrderItem item,
                                          List<Long> removeIngredientIds,
                                          List<Long> addIngredientIds) {
        if (item.getProduct().getType() != ProductType.PIZZA) {
            throw new BadRequestException(
                    "Only pizza items support customizations",
                    ErrorCode.INVALID_OPERATION,
                    ErrorContext.of("orderItemId", item.getId())
            );
        }

        Pizza pizza = pizzaRepository.findByProduct(item.getProduct())
                .orElseThrow(() -> new NotFoundException(
                        "Pizza entity not found for product",
                        ErrorCode.PIZZA_NOT_FOUND,
                        ErrorContext.of("productId", item.getProduct().getId())
                ));

        item.getCustomizations().clear();
        orderItemCustomizationRepository.deleteAll(
                orderItemCustomizationRepository.findByOrderItem(item)
        );

        BigDecimal extrasSum = applyPizzaCustomizations(item, pizza, removeIngredientIds, addIngredientIds);

        BigDecimal base = nz(item.getProduct().getBasePrice());
        BigDecimal variantExtra = (item.getPizzaVariant() != null)
                ? nz(item.getPizzaVariant().getExtraPrice())
                : BigDecimal.ZERO;

        item.setUnitPrice(base.add(variantExtra).add(extrasSum));
    }

    private BigDecimal applyPizzaCustomizations(
            OrderItem item,
            Pizza pizza,
            List<Long> removeIngredientIds,
            List<Long> addIngredientIds
    ) {
        removeIngredientIds = removeIngredientIds != null ? removeIngredientIds : List.of();
        addIngredientIds = addIngredientIds != null ? addIngredientIds : List.of();

        var removeSet = new HashSet<>(removeIngredientIds);
        var addSet = new HashSet<>(addIngredientIds);

        for (Long id : removeSet) {
            if (addSet.contains(id)) {
                throw new BadRequestException(
                        "Ingredient cannot be both added and removed",
                        ErrorCode.INVALID_CUSTOMIZATION,
                        ErrorContext.of("ingredientId", id)
                );
            }
        }

        var allIds = new HashSet<Long>();
        allIds.addAll(removeSet);
        allIds.addAll(addSet);

        if (!allIds.isEmpty()) {
            var ingredients = ingredientRepository.findAllById(allIds);

            Map<Long, Ingredient> byId = ingredients.stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toMap(Ingredient::getId, it -> it, (a, b) -> a));

            for (Long id : allIds) {
                Ingredient ing = byId.get(id);
                if (ing == null) {
                    throw new NotFoundException(
                            "Some ingredients do not exist",
                            ErrorCode.INGREDIENT_NOT_FOUND,
                            ErrorContext.of("ingredientId", id)
                    );
                }
                if (ing.getDeletedAt() != null) {
                    throw new NotFoundException(
                            "Ingredient is deleted",
                            ErrorCode.INGREDIENT_NOT_FOUND,
                            ErrorContext.of("ingredientId", id)
                    );
                }
            }
        }

        var baseByIngredientId = pizza.getIngredients()
                .stream()
                .collect(Collectors.toMap(
                        pizzaIngredient -> pizzaIngredient.getIngredient().getId(),
                        pizzaIngredient -> pizzaIngredient
                ));

        var allowedByIngredientId = pizza.getAllowedIngredients()
                .stream()
                .collect(Collectors.toMap(
                        pizzaAllowedIngredient -> pizzaAllowedIngredient.getIngredient().getId(),
                        pizzaAllowedIngredient -> pizzaAllowedIngredient
                ));

        for (Long ingredientId : removeSet) {
            var base = baseByIngredientId.get(ingredientId);
            if (base == null) {
                throw new BadRequestException(
                        "Ingredient is not in base recipe",
                        ErrorCode.INVALID_CUSTOMIZATION,
                        ErrorContext.of("ingredientId", ingredientId)
                );
            }
            if (!base.isRemovable()) {
                throw new ConflictException(
                        "Ingredient cannot be removed",
                        ErrorCode.INGREDIENT_NOT_REMOVABLE,
                        ErrorContext.of("ingredientId", ingredientId)
                );
            }

            Ingredient ingredient = base.getIngredient();

            OrderItemCustomization customization = OrderItemCustomization.builder()
                    .orderItem(item)
                    .ingredient(ingredient)
                    .action(OrderItemCustomizationAction.REMOVE)
                    .build();

            item.getCustomizations().add(customization);
        }

        BigDecimal extrasSum = BigDecimal.ZERO;

        for (Long ingId : addSet) {
            var allowed = allowedByIngredientId.get(ingId);
            if (allowed == null) {
                throw new BadRequestException(
                        "Ingredient is not allowed for this pizza",
                        ErrorCode.INVALID_CUSTOMIZATION,
                        ErrorContext.of("ingredientId", ingId)
                );
            }

            Ingredient ingredient = allowed.getIngredient();
            BigDecimal extra = nz(allowed.getExtraPrice());
            extrasSum = extrasSum.add(extra);

            OrderItemCustomization customization = OrderItemCustomization.builder()
                    .orderItem(item)
                    .ingredient(ingredient)
                    .action(OrderItemCustomizationAction.ADD)
                    .build();

            item.getCustomizations().add(customization);
        }

        orderItemCustomizationRepository.saveAll(item.getCustomizations());
        return extrasSum;
    }

    private void recalcPizzaUnitPrice(OrderItem item, Pizza pizza) {
        BigDecimal base = nz(item.getProduct().getBasePrice());
        BigDecimal variantExtra = (item.getPizzaVariant() != null) ? nz(item.getPizzaVariant().getExtraPrice()) : BigDecimal.ZERO;

        Map<Long, BigDecimal> allowedExtraByIngredientId = pizza.getAllowedIngredients().stream()
                .collect(Collectors.toMap(
                        pai -> pai.getIngredient().getId(),
                        pai -> nz(pai.getExtraPrice())
                ));

        BigDecimal extrasSum = item.getCustomizations().stream()
                .filter(customization -> customization.getAction() == OrderItemCustomizationAction.ADD)
                .map(customization -> {
                    if (customization.getIngredient() == null) return BigDecimal.ZERO;
                    return allowedExtraByIngredientId.getOrDefault(customization.getIngredient().getId(), BigDecimal.ZERO);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        item.setUnitPrice(base.add(variantExtra).add(extrasSum));
    }

    private void ensureCanModifyOrder(Order order, User currentUser, String guestToken) {
        if (currentUser != null) {
            if (order.getUser() == null || !order.getUser().getId().equals(currentUser.getId())) {
                throw new ForbiddenException(
                        "You cannot modify another user's cart",
                        ErrorCode.CART_FORBIDDEN
                );
            }
        } else {
            if (order.getUser() != null) {
                throw new ForbiddenException(
                        "You cannot modify a user cart as guest",
                        ErrorCode.CART_FORBIDDEN
                );
            }
            if (!StringUtils.hasText(guestToken) || !Objects.equals(guestToken, order.getGuestToken())) {
                throw new ForbiddenException(
                        "You cannot modify another guest cart",
                        ErrorCode.CART_FORBIDDEN
                );
            }
        }
    }

    private Order mergeGuestCartIntoUserCart(User user, String guestToken) {
        if (!StringUtils.hasText(guestToken)) {
            return getOrCreateUserCart(user);
        }

        Optional<Order> guestCartOpt = orderRepository
                .findFirstByGuestTokenAndStatusOrderByIdDesc(guestToken, OrderStatus.CART);

        if (guestCartOpt.isEmpty()) {
            return getOrCreateUserCart(user);
        }

        Order guestCart = guestCartOpt.get();
        Optional<Order> userCartOpt = orderRepository
                .findFirstByUserAndStatusOrderByIdDesc(user, OrderStatus.CART);

        if (userCartOpt.isEmpty()) {
            guestCart.setUser(user);
            guestCart.setGuestToken(null);
            guestCart.setUpdatedAt(LocalDateTime.now());
            return orderRepository.save(guestCart);
        }

        Order userCart = userCartOpt.get();

        if (guestCart.getItems() != null && !guestCart.getItems().isEmpty()) {
            List<OrderItem> guestItems = new ArrayList<>(guestCart.getItems());

            for (OrderItem guestItem : guestItems) {
                guestCart.getItems().remove(guestItem);
                guestItem.setOrder(userCart);
                userCart.getItems().add(guestItem);
            }
        }

        userCart.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(userCart);
        orderRepository.delete(guestCart);
        return userCart;
    }

    private void requirePositiveQty(int quantity) {
        if (quantity <= 0) {
            throw new BadRequestException(
                    "Quantity must be positive",
                    ErrorCode.INVALID_QUANTITY,
                    ErrorContext.of("quantity", quantity)
            );
        }
    }

    private void requireGuestToken(String guestToken) {
        if (!StringUtils.hasText(guestToken)) {
            throw new BadRequestException(
                    "Guest token is required",
                    ErrorCode.GUEST_TOKEN_REQUIRED
            );
        }
    }

    private void requireId(Long id, String field, String code, String message) {
        if (id == null || id <= 0) {
            throw new BadRequestException(
                    message,
                    code,
                    ErrorContext.of(field, id)
            );
        }
    }

    private Product requireProduct(Long productId) {
        requireId(productId, "productId", ErrorCode.INVALID_PRODUCT_ID, "Invalid product id");

        return productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException(
                        "Product not found",
                        ErrorCode.PRODUCT_NOT_FOUND,
                        ErrorContext.of("productId", productId)
                ));
    }

    private OrderItem requireOrderItem(Long orderItemId) {
        requireId(orderItemId, "orderItemId", ErrorCode.INVALID_ORDER_ITEM_ID, "Invalid order item id");

        return orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new NotFoundException(
                        "Order item not found",
                        ErrorCode.ORDER_ITEM_NOT_FOUND,
                        ErrorContext.of("orderItemId", orderItemId)
                ));
    }

    private void ensureProductNotDeleted(Product p) {
        if (p.getDeletedAt() != null) {
            throw new ConflictException(
                    "Product is deleted",
                    ErrorCode.PRODUCT_DELETED,
                    ErrorContext.of("productId", p.getId())
            );
        }
    }

    private void ensureType(Product p, ProductType expected) {
        if (p.getType() != expected) {
            throw new BadRequestException(
                    "Invalid product type",
                    ErrorCode.INVALID_PRODUCT_TYPE,
                    ctx("productId", p.getId(), "expected", expected.name(), "actual", p.getType().name())
            );
        }
    }

    private BigDecimal nz(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }

    private Map<String, Object> ctx(Object... kv) {
        Map<String, Object> m = new HashMap<>();
        if (kv == null) return m;
        for (int i = 0; i + 1 < kv.length; i += 2) {
            Object k = kv[i];
            Object v = kv[i + 1];
            if (k != null && v != null) {
                m.put(String.valueOf(k), v);
            }
        }
        return m;
    }
}
