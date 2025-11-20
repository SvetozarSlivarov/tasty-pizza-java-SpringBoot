package bg.svetozar.tastypizza.repository;

import bg.svetozar.tastypizza.model.entity.Product;
import bg.svetozar.tastypizza.model.enums.ProductType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("select p.basePrice from Product p where p.id = :id")
    Optional<BigDecimal> findBasePriceById(Long id);

    @Query("select p.type from Product p where p.id = :id")
    Optional<ProductType> findTypeById(Long id);
}
