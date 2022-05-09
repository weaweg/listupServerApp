package pz.gr3.serwer.controllers;

import pz.gr3.serwer.repositories.*;
import pz.gr3.serwer.tables.*;
import pz.gr3.serwer.CustomResponse;
import pz.gr3.serwer.CustomAuth;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("tasks")
public class TaskController {
    private Group group;
    private Task task;
    private final TaskRepository repo;
    private final GroupRepository groupRepo;
    private final CheckListRepository listRepo;

    public TaskController(TaskRepository repo, GroupRepository groupRepo, CheckListRepository listRepo) {
        this.repo = repo;
        this.groupRepo = groupRepo;
        this.listRepo = listRepo;
    }

    @PutMapping()
    public ResponseEntity<?> addTask(@RequestParam Integer listId, @RequestParam String desc, HttpServletRequest request) {
        if (desc.equals(""))
            return new ResponseEntity<>(new CustomResponse("Description cannot be empty"), HttpStatus.BAD_REQUEST);
        Integer clientId = CustomAuth.getUserId(request);
        if (clientId == null)
            return new ResponseEntity<>(new CustomResponse("Not logged in"), HttpStatus.UNAUTHORIZED);
        CheckList list = listRepo.findById(listId).orElse(null);
        if(list == null)
            return new ResponseEntity<>(new CustomResponse("List not found"), HttpStatus.NOT_FOUND);
        Group group = groupRepo.findGroupWithUsers(list.getGroup().getGroup_id()).orElseThrow();
        if(group.getUsers().stream().noneMatch(e -> e.getUser_id().equals(clientId)))
            return new ResponseEntity<>(new CustomResponse("Client not in list's group"), HttpStatus.FORBIDDEN);
        Task task = new Task(desc, list, false);
        task = repo.save(task);
        return new ResponseEntity<>(task, HttpStatus.OK);
    }

    @PatchMapping("{taskId}")
    public ResponseEntity<?> changeTask(@PathVariable(required = false) Integer taskId,
                                        @RequestParam(required = false) String desc,
                                        @RequestParam(required = false) Boolean status, HttpServletRequest request) {
        ResponseEntity<?> response = checkIfClientInGroup(taskId, request);
        if(response != null)
            return response;
        if(desc != null && !desc.equals("")) {
            if(repo.changeTaskDesc(taskId, desc) == 0)
                throw new EntityNotFoundException();
            return new ResponseEntity<>(new CustomResponse("Task's description changed"), HttpStatus.OK);
        }
        if(status != null) {
            if (repo.changeTaskStatus(taskId, status) == 0)
                throw new EntityNotFoundException();
            return new ResponseEntity<>(new CustomResponse("Task status changed"), HttpStatus.OK);
        }
        return new ResponseEntity<>(new CustomResponse("Not valid params"), HttpStatus.BAD_REQUEST);
    }

    @DeleteMapping()
    public ResponseEntity<?> deleteList(@RequestParam Integer taskId, HttpServletRequest request) {
        ResponseEntity<?> response = checkIfClientInGroup(taskId, request);
        if(response != null)
            return response;
        repo.deleteById(taskId);
        return new ResponseEntity<>(new CustomResponse("List deleted"), HttpStatus.OK);
    }

    @PostMapping("{taskId}")
    public ResponseEntity<?> changeUserAssigned(@PathVariable Integer taskId, @RequestParam Integer userId, HttpServletRequest request) {
        ResponseEntity<?> response = checkIfClientInGroup(taskId, request);
        if(response != null)
            return response;
        if(group.getUsers().stream().noneMatch(e -> e.getUser_id().equals(userId)))
            return new ResponseEntity<>(new CustomResponse("Cannot assign user not in list's group"), HttpStatus.CONFLICT);
        if(repo.changeTaskUser(taskId, userId) == 0)
                throw new EntityNotFoundException();
        return new ResponseEntity<>(new CustomResponse("User assigned to task"), HttpStatus.OK);
    }

    @GetMapping()
    public ResponseEntity<?> getTask(@RequestParam Integer taskId, HttpServletRequest request) {
        ResponseEntity<?> response = checkIfClientInGroup(taskId, request);
        if(response != null)
            return response;
        return new ResponseEntity<>(task, HttpStatus.OK);
    }

    private ResponseEntity<?> checkIfClientInGroup(Integer taskId, HttpServletRequest request) {
        Integer clientId = CustomAuth.getUserId(request);
        if (clientId == null)
            return new ResponseEntity<>(new CustomResponse("Not logged in"), HttpStatus.UNAUTHORIZED);
        task = repo.findById(taskId).orElse(null);
        if(task == null)
            return new ResponseEntity<>(new CustomResponse("Task not found"), HttpStatus.NOT_FOUND);
        group = groupRepo.findGroupWithUsers(task.getList().getGroup().getGroup_id()).orElseThrow();
        if(group.getUsers().stream().noneMatch(e -> e.getUser_id().equals(clientId)))
            return new ResponseEntity<>(new CustomResponse("Client not in list's group"), HttpStatus.FORBIDDEN);
        return null;
    }
}
