package bg.svetozar.tastypizza.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "drinks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Drink {

    @Id
    @Column(name = "product_id")
    private Long id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "product_id")
    private Product product;
}
