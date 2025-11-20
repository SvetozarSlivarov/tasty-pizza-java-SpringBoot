package bg.svetozar.tastypizza.repository;

import bg.svetozar.tastypizza.model.entity.Drink;
import bg.svetozar.tastypizza.model.entity.Pizza;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DrinkRepository extends JpaRepository<Drink, Long> {

    @Query("""
        SELECT d FROM Drink d
        JOIN FETCH d.product prod
        WHERE prod.deleted = false
    """)
    List<Drink> findAllLight();

    @Query("""
        SELECT d FROM Drink d
        JOIN FETCH d.product prod
        WHERE prod.deleted = false AND d.id = :id
    """)
    Optional<Drink> findByIdLight(long id);
}
