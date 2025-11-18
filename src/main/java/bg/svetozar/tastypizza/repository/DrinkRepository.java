package bg.svetozar.tastypizza.repository;

import bg.svetozar.tastypizza.model.entity.Drink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DrinkRepository extends JpaRepository<Drink, Long> {

    List<Drink> findByProduct_AvailableTrue();
}
