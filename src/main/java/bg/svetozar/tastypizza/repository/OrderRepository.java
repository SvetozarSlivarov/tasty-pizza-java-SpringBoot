package bg.svetozar.tastypizza.repository;

import bg.svetozar.tastypizza.model.entity.Order;
import bg.svetozar.tastypizza.model.entity.User;
import bg.svetozar.tastypizza.model.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserOrderByCreatedAtDesc(User user);

    @Query("""
  select distinct o
  from Order o
  left join fetch o.items i
  left join fetch i.product p
  left join fetch i.pizzaVariant pv
  where o.user = :user
    and o.status <> bg.svetozar.tastypizza.model.enums.OrderStatus.CART
  order by o.createdAt desc
""")
    List<Order> findMyOrdersWithItems(@Param("user") User user);

    Optional<Order> findFirstByUserAndStatusOrderByIdDesc(User user, OrderStatus status);

    Optional<Order> findFirstByGuestTokenAndStatusOrderByIdDesc(String guestToken, OrderStatus status);
}
