package bg.svetozar.tastypizza.repository;

import bg.svetozar.tastypizza.model.entity.Order;
import bg.svetozar.tastypizza.model.entity.User;
import bg.svetozar.tastypizza.model.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserOrderByCreatedAtDesc(User user);

    Optional<Order> findFirstByUserAndStatusOrderByIdDesc(User user, OrderStatus status);

    Optional<Order> findFirstByGuestTokenAndStatusOrderByIdDesc(String guestToken, OrderStatus status);
}
