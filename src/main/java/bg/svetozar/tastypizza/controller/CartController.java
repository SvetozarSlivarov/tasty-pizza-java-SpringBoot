package bg.svetozar.tastypizza.controller;

import bg.svetozar.tastypizza.model.dto.order.*;
import bg.svetozar.tastypizza.service.CartService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import static bg.svetozar.tastypizza.exception.ErrorMessage.INVALID_ITEM_ID_POSITIVE;

import java.time.Duration;
import java.util.UUID;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Validated
public class CartController {

    private static final String CART_TOKEN_COOKIE = "cart_token";

    private final CartService cartService;

    private String ensureCartTokenCookie(String cartTokenFromCookie, HttpServletResponse response) {
        if (StringUtils.hasText(cartTokenFromCookie)) {
            return cartTokenFromCookie;
        }

        String newToken = UUID.randomUUID().toString();

        ResponseCookie cookie = ResponseCookie.from(CART_TOKEN_COOKIE, newToken)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(Duration.ofDays(30))
                .sameSite("Lax")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return newToken;
    }

    @GetMapping
    public ResponseEntity<CartDto> getCart(
            @CookieValue(name = CART_TOKEN_COOKIE, required = false) String cartToken,
            HttpServletResponse response,
            @RequestParam(value = "lang", required = false) String lang
    ) {
        String guestToken = ensureCartTokenCookie(cartToken, response);
        return ResponseEntity.ok(cartService.getCurrentCart(guestToken, lang));
    }

    @PostMapping("/items/drink")
    public ResponseEntity<CartDto> addDrinkToCart(
            @CookieValue(name = CART_TOKEN_COOKIE, required = false) String cartToken,
            HttpServletResponse response,
            @RequestParam(value = "lang", required = false) String lang,
            @Valid @RequestBody AddDrinkToCartRequest request
    ) {
        String guestToken = ensureCartTokenCookie(cartToken, response);

        CartDto cart = cartService.addDrinkToCart(
                guestToken,
                request.productId(),
                request.quantity(),
                request.note(),
                lang
        );

        return ResponseEntity.ok(cart);
    }

    @PostMapping("/items/pizza")
    public ResponseEntity<CartDto> addPizzaToCart(
            @CookieValue(name = CART_TOKEN_COOKIE, required = false) String cartToken,
            HttpServletResponse response,
            @RequestParam(value = "lang", required = false) String lang,
            @Valid @RequestBody AddPizzaToCartRequest request
    ) {
        String guestToken = ensureCartTokenCookie(cartToken, response);

        CartDto cart = cartService.addPizzaToCart(
                guestToken,
                request.productId(),
                request.variantId(),
                request.quantity(),
                request.note(),
                request.removeIngredientIds(),
                request.addIngredientIds(),
                lang
        );

        return ResponseEntity.ok(cart);
    }

    @PostMapping("/items/pasta")
    public ResponseEntity<CartDto> addPastaToCart(
            @CookieValue(name = CART_TOKEN_COOKIE, required = false) String cartToken,
            HttpServletResponse response,
            @RequestParam(value = "lang", required = false) String lang,
            @Valid @RequestBody AddPastaToCartRequest request
    ) {
        String guestToken = ensureCartTokenCookie(cartToken, response);

        CartDto cart = cartService.addPastaToCart(
                guestToken,
                request.productId(),
                request.pastaSauceId(),
                request.quantity(),
                request.note(),
                request.addIngredientIds(),
                lang
        );

        return ResponseEntity.ok(cart);
    }

    @PatchMapping("/items/{itemId}")
    public ResponseEntity<CartDto> updateCartItem(
            @CookieValue(name = CART_TOKEN_COOKIE, required = false) String cartToken,
            HttpServletResponse response,
            @PathVariable @Min(value = 1, message = INVALID_ITEM_ID_POSITIVE) Long itemId,
            @RequestParam(value = "lang", required = false) String lang,
            @Valid @RequestBody UpdateCartItemRequest request
    ) {
        String guestToken = ensureCartTokenCookie(cartToken, response);
        return ResponseEntity.ok(cartService.patchCartItem(guestToken, itemId, request, lang));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<CartDto> removeItem(
            @CookieValue(name = CART_TOKEN_COOKIE, required = false) String cartToken,
            HttpServletResponse response,
            @PathVariable @Min(value = 1, message = INVALID_ITEM_ID_POSITIVE) Long itemId,
            @RequestParam(value = "lang", required = false) String lang
    ) {
        String guestToken = ensureCartTokenCookie(cartToken, response);
        return ResponseEntity.ok(cartService.removeItem(guestToken, itemId, lang));
    }

    @PostMapping("/checkout")
    public ResponseEntity<CartDto> checkout(
            @CookieValue(name = CART_TOKEN_COOKIE, required = false) String cartToken,
            HttpServletResponse response,
            @RequestParam(value = "lang", required = false) String lang,
            @Valid @RequestBody CheckoutRequest request
    ) {
        String guestToken = ensureCartTokenCookie(cartToken, response);

        CartDto cart = cartService.checkout(
                guestToken,
                request.phone(),
                request.address(),
                lang
        );

        return ResponseEntity.ok(cart);
    }
}
