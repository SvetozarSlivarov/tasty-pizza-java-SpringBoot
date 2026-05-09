package bg.svetozar.tastypizza.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pizza_variant_id")
    private PizzaVariant pizzaVariant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pasta_sauce_id")
    private PastaSauce pastaSauce;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "unit_price", nullable = false, precision = 8, scale = 2)
    private BigDecimal unitPrice;

    @Column(columnDefinition = "text")
    private String note;

    @OneToMany(mappedBy = "orderItem", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 20)
    @Builder.Default
    private List<OrderItemCustomization> customizations = new ArrayList<>();
}
