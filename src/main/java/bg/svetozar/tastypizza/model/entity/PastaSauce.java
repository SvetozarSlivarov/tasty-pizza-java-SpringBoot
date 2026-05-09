package bg.svetozar.tastypizza.model.entity;

import bg.svetozar.tastypizza.model.enums.SpicyLevel;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "pasta_sauces")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PastaSauce {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pasta_id", nullable = false)
    private Pasta pasta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;

    @Column(name = "extra_price", nullable = false, precision = 8, scale = 2)
    private BigDecimal extraPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "spicy_level", nullable = false, length = 20)
    private SpicyLevel spicyLevel;
}
