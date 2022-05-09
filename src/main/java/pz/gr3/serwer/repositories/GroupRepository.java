package pz.gr3.serwer.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import pz.gr3.serwer.tables.Group;

import java.util.Optional;

public interface GroupRepository extends JpaRepository<Group, Integer> {
    @Query("SELECT g FROM Group g LEFT JOIN FETCH g.users WHERE g.group_id = ?1")
    Optional<Group> findGroupWithUsers(Integer id);

    @Query("SELECT g FROM Group g LEFT JOIN FETCH g.lists WHERE g.group_id = ?1")
    Optional<Group> findGroupWithLists(Integer id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE Group g SET g.name = ?2 WHERE g.group_id = ?1")
    int changeGroupName(Integer id, String name);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM users_in_groups WHERE user_id = ?1", nativeQuery = true)
    int deleteUserFromGroup(Integer id);
}