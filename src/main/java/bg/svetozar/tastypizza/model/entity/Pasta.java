package bg.svetozar.tastypizza.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pastas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pasta {

    @Id
    @Column(name = "product_id")
    private Long id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "product_id")
    private Product product;

    @OneToMany(mappedBy = "pasta", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PastaSauce> sauces = new ArrayList<>();

    @OneToMany(mappedBy = "pasta", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PastaAllowedIngredient> allowedIngredients = new ArrayList<>();
}
