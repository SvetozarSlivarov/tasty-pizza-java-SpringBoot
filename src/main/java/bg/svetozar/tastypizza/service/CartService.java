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
import bg.svetozar.tastypizza.util.ValidationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static bg.svetozar.tastypizza.exception.ErrorMessage.CANNOT_CHECKOUT_EMPTY_CART;
import static bg.svetozar.tastypizza.exception.ErrorMessage.CANNOT_MODIFY_ANOTHER_GUEST_CART;
import static bg.svetozar.tastypizza.exception.ErrorMessage.CANNOT_MODIFY_ANOTHER_USER_CART;
import static bg.svetozar.tastypizza.exception.ErrorMessage.CANNOT_MODIFY_USER_CART;
import static bg.svetozar.tastypizza.exception.ErrorMessage.GUEST_TOKEN_REQUIRED;
import static bg.svetozar.tastypizza.exception.ErrorMessage.INGREDIENT_CANNOT_BE_BOTH_ADDED_AND_REMOVED;
import static bg.svetozar.tastypizza.exception.ErrorMessage.INGREDIENT_DELETED;
import static bg.svetozar.tastypizza.exception.ErrorMessage.INGREDIENT_IS_NOT_IN_RECIPE;
import static bg.svetozar.tastypizza.exception.ErrorMessage.INGREDIENT_NOT_ALLOWED;
import static bg.svetozar.tastypizza.exception.ErrorMessage.INGREDIENT_NOT_REMOVABLE;
import static bg.svetozar.tastypizza.exception.ErrorMessage.INGREDIENTS_DO_NOT_EXIST;
import static bg.svetozar.tastypizza.exception.ErrorMessage.INVALID_PRODUCT_TYPE;
import static bg.svetozar.tastypizza.exception.ErrorMessage.ONLY_PIZZA_CHANGE_VARIANT;
import static bg.svetozar.tastypizza.exception.ErrorMessage.ONLY_PIZZA_CUSTOMIZATION;
import static bg.svetozar.tastypizza.exception.ErrorMessage.ORDER_IS_NOT_CART;
import static bg.svetozar.tastypizza.exception.ErrorMessage.ORDER_ITEM_NOT_FOUND;
import static bg.svetozar.tastypizza.exception.ErrorMessage.PIZZA_ENTITY_NOT_FOUND_PRODUCT;
import static bg.svetozar.tastypizza.exception.ErrorMessage.PIZZA_VARIANT_NOT_FOUND;
import static bg.svetozar.tastypizza.exception.ErrorMessage.PIZZA_VARIANT_NOT_FOUND_FOR_PIZZA;
import static bg.svetozar.tastypizza.exception.ErrorMessage.POSITIVE_QUANTITY;
import static bg.svetozar.tastypizza.exception.ErrorMessage.PRODUCT_DELETED;
import static bg.svetozar.tastypizza.exception.ErrorMessage.PRODUCT_NOT_FOUND;
import static bg.svetozar.tastypizza.exception.ErrorMessage.REQUIRED_ADDRESS;
import static bg.svetozar.tastypizza.exception.ErrorMessage.REQUIRED_PHONE;
import static bg.svetozar.tastypizza.exception.ErrorMessage.REQUIRED_VARIANTS;
import static bg.svetozar.tastypizza.exception.ErrorMessage.INVALID_PRODUCT_ID;
import static bg.svetozar.tastypizza.exception.ErrorMessage.INVALID_ORDER_ITEM_ID;

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

        Order order = resolveCurrentCart(guestToken);

        Product product = requireActiveProductOfType(drinkProductId, ProductType.DRINK);
        createAndSaveDrinkItem(order, product, quantity, note);

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
        requireId(variantId, "variantId", ErrorCode.VARIANT_REQUIRED, REQUIRED_VARIANTS);

        Order order = resolveCurrentCart(guestToken);

        Product product = requireActiveProductOfType(pizzaProductId, ProductType.PIZZA);
        PizzaAndVariant pv = requirePizzaAndVariant(product, variantId);

        BigDecimal unitPrice = calcPizzaBaseUnitPrice(product, pv.variant);
        OrderItem item = createAndSavePizzaItem(order, product, pv.variant, quantity, note, unitPrice);

        BigDecimal extrasSum = applyPizzaCustomizations(item, pv.pizza, removeIngredientIds, addIngredientIds);
        item.setUnitPrice(unitPrice.add(extrasSum));

        attachItemToOrderAndTouch(order, item);

        return OrderMapper.toCartDto(order);
    }

    public CartDto patchCartItem(String guestToken, Long orderItemId, UpdateCartItemRequest request) {
        User user = getCurrentUserOrNull();

        OrderItem item = requireOrderItem(orderItemId);
        Order order = item.getOrder();
        ensureCanModifyOrder(order, user, guestToken);

        boolean changed = false;

        changed |= applyIfPresent(request.quantity(), qty -> {
            requirePositiveQty(qty);
            item.setQuantity(qty);
        });

        changed |= applyIfPresent(request.note(), item::setNote);

        changed |= applyIfPresent(request.variantId(), variantId ->
                applyVariantChange(item, variantId)
        );

        changed |= applyIfAnyPresent(request.removeIngredientIds(), request.addIngredientIds(), () ->
                applyCustomizationChange(item, request.removeIngredientIds(), request.addIngredientIds())
        );

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
        Order cart = resolveCurrentCartForCheckout(guestToken);

        requireCartStatusCart(cart);
        requireCheckoutFields(phone, address);
        requireCartNotEmpty(cart);

        applyCheckout(cart, phone, address);

        return OrderMapper.toCartDto(cart);
    }

    public CartDto addDrinkToExistingCart(Order order, Long drinkProductId, int quantity, String note) {
        requirePositiveQty(quantity);

        Product product = requireActiveProductOfType(drinkProductId, ProductType.DRINK);
        createAndSaveDrinkItem(order, product, quantity, note);

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
        requireId(variantId, "variantId", ErrorCode.VARIANT_REQUIRED, REQUIRED_VARIANTS);

        Product product = requireActiveProductOfType(pizzaProductId, ProductType.PIZZA);
        PizzaAndVariant pv = requirePizzaAndVariant(product, variantId);

        BigDecimal unitPrice = calcPizzaBaseUnitPrice(product, pv.variant);
        OrderItem item = createAndSavePizzaItem(order, product, pv.variant, quantity, note, unitPrice);

        BigDecimal extrasSum = applyPizzaCustomizations(item, pv.pizza, removeIngredientIds, addIngredientIds);
        item.setUnitPrice(unitPrice.add(extrasSum));

        attachItemToOrderAndTouch(order, item);

        return OrderMapper.toCartDto(order);
    }

    private Order resolveCurrentCart(String guestToken) {
        User user = getCurrentUserOrNull();
        return (user != null)
                ? mergeGuestCartIntoUserCart(user, guestToken)
                : getOrCreateGuestCart(guestToken);
    }

    private Order resolveCurrentCartForCheckout(String guestToken) {
        User user = getCurrentUserOrNull();
        if (user != null) return mergeGuestCartIntoUserCart(user, guestToken);

        requireGuestToken(guestToken);
        return getOrCreateGuestCart(guestToken);
    }

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

    private Product requireActiveProductOfType(Long productId, ProductType expectedType) {
        Product product = requireProduct(productId);
        ensureProductNotDeleted(product);
        ensureType(product, expectedType);
        return product;
    }

    private OrderItem createAndSaveDrinkItem(Order order, Product product, int quantity, String note) {
        BigDecimal unitPrice = nullToZero(product.getBasePrice());

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

        return item;
    }

    private BigDecimal calcPizzaBaseUnitPrice(Product product, PizzaVariant variant) {
        BigDecimal base = nullToZero(product.getBasePrice());
        BigDecimal variantExtra = nullToZero(variant.getExtraPrice());
        return base.add(variantExtra);
    }

    private OrderItem createAndSavePizzaItem(
            Order order,
            Product product,
            PizzaVariant variant,
            int quantity,
            String note,
            BigDecimal unitPrice
    ) {
        OrderItem item = OrderItem.builder()
                .order(order)
                .product(product)
                .pizzaVariant(variant)
                .quantity(quantity)
                .unitPrice(unitPrice)
                .note(note)
                .build();

        orderItemRepository.save(item);
        return item;
    }

    private void attachItemToOrderAndTouch(Order order, OrderItem item) {
        order.getItems().add(item);
        order.setUpdatedAt(LocalDateTime.now());
    }

    private void applyVariantChange(OrderItem item, Long variantId) {
        if (item.getProduct().getType() != ProductType.PIZZA) {
            throw new BadRequestException(
                    ONLY_PIZZA_CHANGE_VARIANT,
                    ErrorCode.INVALID_OPERATION,
                    ErrorContext.of("orderItemId", item.getId())
            );
        }

        requireId(variantId, "variantId", ErrorCode.VARIANT_REQUIRED, REQUIRED_VARIANTS);

        PizzaVariant variant = pizzaVariantRepository.findById(variantId)
                .orElseThrow(() -> new NotFoundException(
                        PIZZA_VARIANT_NOT_FOUND,
                        ErrorCode.VARIANT_NOT_FOUND,
                        ErrorContext.of("variantId", variantId)
                ));


        Long variantProductId = variant.getPizza().getProduct().getId();
        Long itemProductId = item.getProduct().getId();

        if (!Objects.equals(variantProductId, itemProductId)) {
            throw new BadRequestException(
                    PIZZA_VARIANT_NOT_FOUND_FOR_PIZZA,
                    ErrorCode.VARIANT_NOT_BELONG_TO_PIZZA,
                    buildContext("variantId", variantId, "productId", itemProductId)
            );
        }

        item.setPizzaVariant(variant);

        Pizza pizza = pizzaRepository.findByProduct(item.getProduct())
                .orElseThrow(() -> new NotFoundException(
                        PIZZA_ENTITY_NOT_FOUND_PRODUCT,
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
                    ONLY_PIZZA_CUSTOMIZATION,
                    ErrorCode.INVALID_OPERATION,
                    ErrorContext.of("orderItemId", item.getId())
            );
        }

        Pizza pizza = pizzaRepository.findByProduct(item.getProduct())
                .orElseThrow(() -> new NotFoundException(
                        PIZZA_ENTITY_NOT_FOUND_PRODUCT,
                        ErrorCode.PIZZA_NOT_FOUND,
                        ErrorContext.of("productId", item.getProduct().getId())
                ));

        item.getCustomizations().clear();
        orderItemCustomizationRepository.deleteAll(
                orderItemCustomizationRepository.findByOrderItem(item)
        );

        BigDecimal extrasSum = applyPizzaCustomizations(item, pizza, removeIngredientIds, addIngredientIds);

        BigDecimal base = nullToZero(item.getProduct().getBasePrice());
        BigDecimal variantExtra = (item.getPizzaVariant() != null)
                ? nullToZero(item.getPizzaVariant().getExtraPrice())
                : BigDecimal.ZERO;

        item.setUnitPrice(base.add(variantExtra).add(extrasSum));
    }

    private BigDecimal applyPizzaCustomizations(
            OrderItem item,
            Pizza pizza,
            List<Long> removeIngredientIds,
            List<Long> addIngredientIds
    ) {
        var removeSet = toIdSet(removeIngredientIds);
        var addSet = toIdSet(addIngredientIds);

        ensureNoOverlap(removeSet, addSet);

        var allIds = union(removeSet, addSet);
        validateIngredientsExistAndNotDeleted(allIds);

        var baseByIngredientId = mapBaseIngredients(pizza);
        var allowedByIngredientId = mapAllowedIngredients(pizza);

        applyRemoveCustomizations(item, removeSet, baseByIngredientId);
        BigDecimal extrasSum = applyAddCustomizations(item, addSet, allowedByIngredientId);

        orderItemCustomizationRepository.saveAll(item.getCustomizations());
        return extrasSum;
    }

    private Set<Long> toIdSet(List<Long> ids) {
        ids = ids != null ? ids : List.of();
        return new HashSet<>(ids);
    }

    private void ensureNoOverlap(Set<Long> removeSet, Set<Long> addSet) {
        for (Long id : removeSet) {
            if (addSet.contains(id)) {
                throw new BadRequestException(
                        INGREDIENT_CANNOT_BE_BOTH_ADDED_AND_REMOVED,
                        ErrorCode.INVALID_CUSTOMIZATION,
                        ErrorContext.of("ingredientId", id)
                );
            }
        }
    }

    private Set<Long> union(Set<Long> a, Set<Long> b) {
        var all = new HashSet<Long>();
        all.addAll(a);
        all.addAll(b);
        return all;
    }

    private void validateIngredientsExistAndNotDeleted(Set<Long> allIds) {
        if (allIds.isEmpty()) return;

        var ingredients = ingredientRepository.findAllById(allIds);

        Map<Long, Ingredient> byId = ingredients.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Ingredient::getId, ingredient -> ingredient, (x, y) -> x));

        for (Long id : allIds) {
            Ingredient ing = byId.get(id);
            if (ing == null) {
                throw new NotFoundException(
                        INGREDIENTS_DO_NOT_EXIST,
                        ErrorCode.INGREDIENT_NOT_FOUND,
                        ErrorContext.of("ingredientId", id)
                );
            }
            if (ing.getDeletedAt() != null) {
                throw new NotFoundException(
                        INGREDIENT_DELETED,
                        ErrorCode.INGREDIENT_NOT_FOUND,
                        ErrorContext.of("ingredientId", id)
                );
            }
        }
    }

    private Map<Long, PizzaIngredient> mapBaseIngredients(Pizza pizza) {
        return pizza.getIngredients()
                .stream()
                .collect(Collectors.toMap(
                        pizzaIngredient -> pizzaIngredient.getIngredient().getId(),
                        pizzaIngredient -> pizzaIngredient
                ));
    }

    private Map<Long, PizzaAllowedIngredient> mapAllowedIngredients(Pizza pizza) {
        return pizza.getAllowedIngredients()
                .stream()
                .collect(Collectors.toMap(
                        pizzaAllowedIngredient -> pizzaAllowedIngredient.getIngredient().getId(),
                        pizzaAllowedIngredient -> pizzaAllowedIngredient
                ));
    }

    private void applyRemoveCustomizations(
            OrderItem item,
            Set<Long> removeSet,
            Map<Long, PizzaIngredient> baseByIngredientId
    ) {
        for (Long ingredientId : removeSet) {
            var base = baseByIngredientId.get(ingredientId);
            if (base == null) {
                throw new BadRequestException(
                        INGREDIENT_IS_NOT_IN_RECIPE,
                        ErrorCode.INVALID_CUSTOMIZATION,
                        ErrorContext.of("ingredientId", ingredientId)
                );
            }
            if (!base.isRemovable()) {
                throw new ConflictException(
                        INGREDIENT_NOT_REMOVABLE,
                        ErrorCode.INGREDIENT_NOT_REMOVABLE,
                        ErrorContext.of("ingredientId", ingredientId)
                );
            }

            Ingredient ingredient = base.getIngredient();
            item.getCustomizations().add(newCustomization(item, ingredient, OrderItemCustomizationAction.REMOVE));
        }
    }

    private BigDecimal applyAddCustomizations(
            OrderItem item,
            Set<Long> addSet,
            Map<Long, PizzaAllowedIngredient> allowedByIngredientId
    ) {
        BigDecimal extrasSum = BigDecimal.ZERO;

        for (Long ingredientId : addSet) {
            var allowed = allowedByIngredientId.get(ingredientId);
            if (allowed == null) {
                throw new BadRequestException(
                        INGREDIENT_NOT_ALLOWED,
                        ErrorCode.INVALID_CUSTOMIZATION,
                        ErrorContext.of("ingredientId", ingredientId)
                );
            }

            Ingredient ingredient = allowed.getIngredient();
            BigDecimal extra = nullToZero(allowed.getExtraPrice());
            extrasSum = extrasSum.add(extra);

            item.getCustomizations().add(newCustomization(item, ingredient, OrderItemCustomizationAction.ADD));
        }

        return extrasSum;
    }

    private OrderItemCustomization newCustomization(
            OrderItem item,
            Ingredient ingredient,
            OrderItemCustomizationAction action
    ) {
        return OrderItemCustomization.builder()
                .orderItem(item)
                .ingredient(ingredient)
                .action(action)
                .build();
    }

    private void recalcPizzaUnitPrice(OrderItem item, Pizza pizza) {
        BigDecimal base = nullToZero(item.getProduct().getBasePrice());
        BigDecimal variantExtra = (item.getPizzaVariant() != null)
                ? nullToZero(item.getPizzaVariant().getExtraPrice())
                : BigDecimal.ZERO;

        Map<Long, BigDecimal> allowedExtraByIngredientId = pizza.getAllowedIngredients().stream()
                .collect(Collectors.toMap(
                        pizzaAllowedIngredient -> pizzaAllowedIngredient.getIngredient().getId(),
                        pizzaAllowedIngredient -> nullToZero(pizzaAllowedIngredient.getExtraPrice())
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
                        CANNOT_MODIFY_ANOTHER_USER_CART,
                        ErrorCode.CART_FORBIDDEN
                );
            }
        } else {
            if (order.getUser() != null) {
                throw new ForbiddenException(
                        CANNOT_MODIFY_USER_CART,
                        ErrorCode.CART_FORBIDDEN
                );
            }
            if (!StringUtils.hasText(guestToken) || !Objects.equals(guestToken, order.getGuestToken())) {
                throw new ForbiddenException(
                        CANNOT_MODIFY_ANOTHER_GUEST_CART,
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

        if (!ValidationUtils.isNullOrEmpty(guestCart.getItems())) {
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

    private void requireCartStatusCart(Order cart) {
        if (cart.getStatus() != OrderStatus.CART) {
            throw new BadRequestException(
                    ORDER_IS_NOT_CART,
                    ErrorCode.INVALID_OPERATION,
                    ErrorContext.of("orderId", cart.getId())
            );
        }
    }

    private void requireCheckoutFields(String phone, String address) {
        if (!StringUtils.hasText(phone)) {
            throw new BadRequestException(
                    REQUIRED_PHONE,
                    ErrorCode.BAD_REQUEST,
                    ErrorContext.of("field", "phone")
            );
        }
        if (!StringUtils.hasText(address)) {
            throw new BadRequestException(
                    REQUIRED_ADDRESS,
                    ErrorCode.BAD_REQUEST,
                    ErrorContext.of("field", "address")
            );
        }
    }

    private void requireCartNotEmpty(Order cart) {
        if (!ValidationUtils.isNullOrEmpty(cart.getItems())) {
            throw new ConflictException(
                    CANNOT_CHECKOUT_EMPTY_CART,
                    ErrorCode.CART_EMPTY
            );
        }
    }

    private void applyCheckout(Order cart, String phone, String address) {
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
    }

    private void requirePositiveQty(int quantity) {
        if (quantity <= 0) {
            throw new BadRequestException(
                    POSITIVE_QUANTITY,
                    ErrorCode.INVALID_QUANTITY,
                    ErrorContext.of("quantity", quantity)
            );
        }
    }

    private void requireGuestToken(String guestToken) {
        if (!StringUtils.hasText(guestToken)) {
            throw new BadRequestException(
                    GUEST_TOKEN_REQUIRED,
                    ErrorCode.GUEST_TOKEN_REQUIRED
            );
        }
    }

    private void requireId(Long id, String field, String code, String message) {
        if (ValidationUtils.isInvalidRequiredId(id)) {
            throw new BadRequestException(
                    message,
                    code,
                    ErrorContext.of(field, id)
            );
        }
    }

    private Product requireProduct(Long productId) {
        requireId(productId, "productId", ErrorCode.INVALID_PRODUCT_ID, INVALID_PRODUCT_ID);

        return productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException(
                        PRODUCT_NOT_FOUND,
                        ErrorCode.PRODUCT_NOT_FOUND,
                        ErrorContext.of("productId", productId)
                ));
    }

    private OrderItem requireOrderItem(Long orderItemId) {
        requireId(orderItemId, "orderItemId", ErrorCode.INVALID_ORDER_ITEM_ID, INVALID_ORDER_ITEM_ID);

        return orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new NotFoundException(
                        ORDER_ITEM_NOT_FOUND,
                        ErrorCode.ORDER_ITEM_NOT_FOUND,
                        ErrorContext.of("orderItemId", orderItemId)
                ));
    }

    private void ensureProductNotDeleted(Product p) {
        if (p.getDeletedAt() != null) {
            throw new ConflictException(
                    PRODUCT_DELETED,
                    ErrorCode.PRODUCT_DELETED,
                    ErrorContext.of("productId", p.getId())
            );
        }
    }

    private void ensureType(Product p, ProductType expected) {
        if (p.getType() != expected) {
            throw new BadRequestException(
                    INVALID_PRODUCT_TYPE,
                    ErrorCode.INVALID_PRODUCT_TYPE,
                    buildContext("productId", p.getId(), "expected", expected.name(), "actual", p.getType().name())
            );
        }
    }

    private static class PizzaAndVariant {
        private final Pizza pizza;
        private final PizzaVariant variant;

        private PizzaAndVariant(Pizza pizza, PizzaVariant variant) {
            this.pizza = pizza;
            this.variant = variant;
        }
    }

    private PizzaAndVariant requirePizzaAndVariant(Product pizzaProduct, Long variantId) {
        Pizza pizza = pizzaRepository.findByProduct(pizzaProduct)
                .orElseThrow(() -> new NotFoundException(
                        PIZZA_ENTITY_NOT_FOUND_PRODUCT,
                        ErrorCode.PIZZA_NOT_FOUND,
                        ErrorContext.of("productId", pizzaProduct.getId())
                ));

        PizzaVariant variant = pizzaVariantRepository.findById(variantId)
                .orElseThrow(() -> new NotFoundException(
                        PIZZA_VARIANT_NOT_FOUND,
                        ErrorCode.VARIANT_NOT_FOUND,
                        ErrorContext.of("variantId", variantId)
                ));

        if (!variant.getPizza().getId().equals(pizza.getId())) {
            throw new BadRequestException(
                    PIZZA_VARIANT_NOT_FOUND_FOR_PIZZA,
                    ErrorCode.VARIANT_NOT_BELONG_TO_PIZZA,
                    buildContext("variantId", variantId, "pizzaId", pizza.getId())
            );
        }

        return new PizzaAndVariant(pizza, variant);
    }

    private BigDecimal nullToZero(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private Map<String, Object> buildContext(Object... keyValuePairs) {
        Map<String, Object> context = new HashMap<>();

        if (keyValuePairs == null) {
            return context;
        }

        for (int i = 0; i + 1 < keyValuePairs.length; i += 2) {
            Object key = keyValuePairs[i];
            Object value = keyValuePairs[i + 1];

            if (key != null && value != null) {
                context.put(String.valueOf(key), value);
            }
        }

        return context;
    }
    private static <T> boolean applyIfPresent(T value, java.util.function.Consumer<T> applier) {
        if (value == null) return false;
        applier.accept(value);
        return true;
    }

    private static boolean applyIfAnyPresent(Object a, Object b, Runnable applier) {
        if (a == null && b == null) return false;
        applier.run();
        return true;
    }
}
