package pz.gr3.serwer.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import pz.gr3.serwer.tables.CheckList;

import java.util.Optional;

public interface CheckListRepository extends JpaRepository<CheckList, Integer> {
    @Query("SELECT l FROM CheckList l WHERE l.group.group_id = ?1 AND l.name = ?2")
    Optional<CheckList> findByNameInGroup(Integer groupId, String name);

    @Query("SELECT l FROM CheckList l LEFT JOIN FETCH l.tasks WHERE l.list_id = ?1")
    Optional<CheckList> findListWithTasks(Integer listId);

    @Query("SELECT l FROM CheckList l LEFT JOIN FETCH l.tasks WHERE l.group.group_id = ?1 AND l.name = ?2")
    Optional<CheckList> findListWithTasksByGroupAndName(Integer groupId, String name);

    @Modifying
    @Transactional
    @Query(value = "UPDATE CheckList l SET l.name = ?2 WHERE l.list_id = ?1")
    int changeListName(Integer listId, String name);
}