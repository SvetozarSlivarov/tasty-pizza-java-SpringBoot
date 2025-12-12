package bg.svetozar.tastypizza.service;

import bg.svetozar.tastypizza.model.dto.order.CartDto;
import bg.svetozar.tastypizza.model.dto.order.OrderStatusChangeDTO;
import bg.svetozar.tastypizza.model.entity.Order;
import bg.svetozar.tastypizza.model.entity.User;
import bg.svetozar.tastypizza.model.enums.OrderStatus;
import bg.svetozar.tastypizza.model.mapper.OrderMapper;
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

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final OrderStatusChangeRepository statusChangeRepository;

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
}
