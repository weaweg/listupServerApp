package pz.gr3.serwer.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pz.gr3.serwer.tables.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.groups WHERE u.user_id = ?1")
    Optional<User> findUserWithGroups(Integer id);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.tasks WHERE u.user_id = ?1")
    Optional<User> findUserWithTasks(Integer id);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.owned_groups WHERE u.user_id = ?1")
    Optional<User> findUserWithOwned(Integer id);
}