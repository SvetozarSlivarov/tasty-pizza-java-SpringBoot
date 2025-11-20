package bg.svetozar.tastypizza.repository;

import bg.svetozar.tastypizza.model.entity.Pizza;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PizzaRepository extends JpaRepository<Pizza, Long> {

    @Query("""
        SELECT p FROM Pizza p
        JOIN FETCH p.product prod
        WHERE prod.deleted = false
    """)
    List<Pizza> findAllLight();


    @Query("""
        SELECT DISTINCT p FROM Pizza p
        JOIN FETCH p.product prod
        LEFT JOIN FETCH p.variants v
        WHERE prod.deleted = false
    """)
    List<Pizza> findAllFull();

    @Query("""
        SELECT p FROM Pizza p
        JOIN FETCH p.product prod
        WHERE prod.deleted = false AND p.id = :id
    """)
    Optional<Pizza> findByIdLight(long id);

    @Query("""
        SELECT DISTINCT p FROM Pizza p
        JOIN FETCH p.product prod
        LEFT JOIN FETCH p.variants v
        WHERE prod.deleted = false AND p.id = :id
    """)
    Optional<Pizza> findByIdFull(long id);


}