package bg.svetozar.tastypizza.model.entity;

import bg.svetozar.tastypizza.model.enums.OrderItemCustomizationAction;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "order_item_customizations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemCustomization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", nullable = false)
    private OrderItem orderItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderItemCustomizationAction action;
}
