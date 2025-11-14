package bg.svetozar.tastypizza.model.entity;

import bg.svetozar.tastypizza.model.enums.DoughType;
import bg.svetozar.tastypizza.model.enums.PizzaSize;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "pizza_variants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PizzaVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pizza_id", nullable = false)
    private Pizza pizza;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PizzaSize size;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DoughType dough;

    @Column(name = "extra_price", precision = 8, scale = 2)
    @Builder.Default
    private BigDecimal extraPrice = BigDecimal.ZERO;
}
