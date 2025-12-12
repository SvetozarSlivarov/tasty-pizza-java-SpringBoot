package bg.svetozar.tastypizza.controller;

import bg.svetozar.tastypizza.model.dto.order.CartDto;
import bg.svetozar.tastypizza.model.dto.order.OrderStatusChangeDTO;
import bg.svetozar.tastypizza.model.dto.order.ReorderResultDto;
import bg.svetozar.tastypizza.service.OrderService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    private static final String CART_TOKEN_COOKIE = "cart_token";

    private String ensureCartTokenCookie(String cartTokenFromCookie, HttpServletResponse response) {
        if (cartTokenFromCookie != null && !cartTokenFromCookie.isBlank()) {
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

    @PostMapping("/{id}/reorder")
    public ResponseEntity<ReorderResultDto> reorder(
            @PathVariable Long id,
            @CookieValue(name = CART_TOKEN_COOKIE, required = false) String cartToken,
            HttpServletResponse response
    ) {
        String guestToken = ensureCartTokenCookie(cartToken, response);
        return ResponseEntity.ok(orderService.reorderIntoCart(id, guestToken));
    }

    @GetMapping("/my")
    public ResponseEntity<List<CartDto>> getMyOrders() {
        return ResponseEntity.ok(orderService.getMyOrders());
    }

    @GetMapping("/{id}/statusHistory")
    public List<OrderStatusChangeDTO> getStatusHistory(@PathVariable Long id,
                                                       Authentication authentication) {
        return orderService.getStatusHistory(id, authentication.getName());
    }
}
