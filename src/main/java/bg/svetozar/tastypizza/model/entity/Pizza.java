package bg.svetozar.tastypizza.model.entity;

import bg.svetozar.tastypizza.model.enums.SpicyLevel;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pizzas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pizza {

    @Id
    @Column(name = "product_id")
    private Long id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "product_id")
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(name = "spicy_level", nullable = false, length = 20)
    private SpicyLevel spicyLevel;

    @OneToMany(mappedBy = "pizza", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PizzaVariant> variants = new ArrayList<>();

    @OneToMany(mappedBy = "pizza", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PizzaIngredient> ingredients = new ArrayList<>();

    @OneToMany(mappedBy = "pizza", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PizzaAllowedIngredient> allowedIngredients = new ArrayList<>();
}
