package bg.svetozar.tastypizza.controller;

import bg.svetozar.tastypizza.model.dto.order.*;
import bg.svetozar.tastypizza.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<CartDto> getCart() {
        CartDto cart = cartService.getCurrentCart();
        return ResponseEntity.ok(cart);
    }

    @PostMapping("/items/drink")
    public ResponseEntity<CartDto> addDrinkToCart(
            @Valid @RequestBody AddDrinkToCartRequest request
    ) {
        CartDto cart = cartService.addDrinkToCart(
                request.productId(),
                request.quantity(),
                request.note()
        );
        return ResponseEntity.ok(cart);
    }

    @PostMapping("/items/pizza")
    public ResponseEntity<CartDto> addPizzaToCart(
            @Valid @RequestBody AddPizzaToCartRequest request
    ) {
        CartDto cart = cartService.addPizzaToCart(
                request.productId(),
                request.variantId(),
                request.quantity(),
                request.note(),
                request.removeIngredientIds(),
                request.addIngredientIds()
        );
        return ResponseEntity.ok(cart);
    }

    @PatchMapping("/items/{itemId}")
    public ResponseEntity<CartDto> updateCartItem(
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateCartItemRequest request
    ) {
        CartDto cart = null;

        if (request.quantity() != null) {
            cart = cartService.updateQuantity(itemId, request.quantity());
        }
        if (request.note() != null) {
            cart = cartService.updateNote(itemId, request.note());
        }
        if (request.variantId() != null) {
            cart = cartService.updateVariant(itemId, request.variantId());
        }
        if (request.removeIngredientIds() != null || request.addIngredientIds() != null) {
            cart = cartService.updatePizzaCustomizations(
                    itemId,
                    request.removeIngredientIds(),
                    request.addIngredientIds()
            );
        }

        if (cart == null) {
            cart = cartService.getCurrentCart();
        }

        return ResponseEntity.ok(cart);
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<CartDto> removeCartItem(
            @PathVariable Long itemId
    ) {
        CartDto cart = cartService.removeItem(itemId);
        return ResponseEntity.ok(cart);
    }

    @PostMapping("/checkout")
    public ResponseEntity<CartDto> checkout(
            @Valid @RequestBody CheckoutRequest request
    ) {
        CartDto cart = cartService.checkout(
                request.phone(),
                request.address()
        );
        return ResponseEntity.ok(cart);
    }
}
