package bg.svetozar.tastypizza.repository;

import bg.svetozar.tastypizza.model.entity.Pizza;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PizzaRepository extends JpaRepository<Pizza, Long> {

    List<Pizza> findByProduct_AvailableTrue();
}