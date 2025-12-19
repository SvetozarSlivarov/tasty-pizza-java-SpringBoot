package bg.svetozar.tastypizza.repository;

import bg.svetozar.tastypizza.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    Optional<User> findByUsernameAndDeletedFalse(String username);

    Optional<User> findByIdAndDeletedFalse(Long id);

    @Query("""
        select u from User u
        where (:q is null or :q = '' or
               lower(u.username) like lower(concat('%', :q, '%')) or
               lower(u.fullname) like lower(concat('%', :q, '%')) or
               cast(u.id as string) = :q
        )
          and (
               :show = 'all'
               or (:show = 'active' and u.deleted = false)
               or (:show = 'deleted' and u.deleted = true)
          )
    """)
    Page<User> adminSearch(@Param("q") String q, @Param("show") String show, Pageable pageable);
}