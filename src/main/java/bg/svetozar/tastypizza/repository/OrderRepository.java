package bg.svetozar.tastypizza.repository;

import bg.svetozar.tastypizza.model.dto.admin.AdminOrderListDto;
import bg.svetozar.tastypizza.model.entity.Order;
import bg.svetozar.tastypizza.model.entity.User;
import bg.svetozar.tastypizza.model.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
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

    @Query(
            value = """
    select new bg.svetozar.tastypizza.model.dto.admin.AdminOrderListDto(
        o.id,
        o.status,
        sum(oi.unitPrice * oi.quantity),
        sum(oi.quantity),
        o.createdAt,
        u.username,
        o.deliveryPhone,
        o.deliveryAddress
    )
    from Order o
    left join o.user u
    left join o.items oi
    where o.status <> bg.svetozar.tastypizza.model.enums.OrderStatus.CART
      and (:status is null or o.status = :status)
      and (
        :q is null or :q = '' or
        cast(o.id as string) like concat('%', :q, '%') or
        lower(u.username) like lower(concat('%', :q, '%')) or
        lower(o.deliveryPhone) like lower(concat('%', :q, '%')) or
        lower(o.deliveryAddress) like lower(concat('%', :q, '%'))
      )
    group by o.id, o.status, o.createdAt, u.username, o.deliveryPhone, o.deliveryAddress
    """,
            countQuery = """
    select count(o)
    from Order o
    left join o.user u
    where o.status <> bg.svetozar.tastypizza.model.enums.OrderStatus.CART
      and (:status is null or o.status = :status)
      and (
        :q is null or :q = '' or
        cast(o.id as string) like concat('%', :q, '%') or
        lower(u.username) like lower(concat('%', :q, '%')) or
        lower(o.deliveryPhone) like lower(concat('%', :q, '%')) or
        lower(o.deliveryAddress) like lower(concat('%', :q, '%'))
      )
    """
    )
    Page<AdminOrderListDto> adminSearch(@Param("status") OrderStatus status,
                                        @Param("q") String q,
                                        Pageable pageable);

    @EntityGraph(attributePaths = {
            "user",
            "items",
            "items.product",
            "items.customizations"
    })
    Optional<Order> findAdminDetailById(Long id);

}
