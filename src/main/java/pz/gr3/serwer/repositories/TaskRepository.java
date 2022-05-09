package pz.gr3.serwer.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import pz.gr3.serwer.tables.Task;

public interface TaskRepository extends JpaRepository<Task, Integer> {
    @Modifying
    @Transactional
    @Query(value = "UPDATE Task t SET t.description = ?2 WHERE t.task_id = ?1")
    int changeTaskDesc(Integer id, String name);

    @Modifying
    @Transactional
    @Query(value = "UPDATE Task t SET t.user.user_id = ?2 WHERE t.task_id = ?1")
    int changeTaskUser(Integer id, Integer userId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE Task t SET t.status = ?2 WHERE t.task_id = ?1")
    int changeTaskStatus(Integer id, boolean status);
}