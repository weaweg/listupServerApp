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
@RequestMapping("lists")
public class CheckListController {
    private Group group;
    private CheckList list;
    private final CheckListRepository repo;
    private final GroupRepository groupRepo;

    public CheckListController(CheckListRepository repo, GroupRepository groupRepo) {
        this.repo = repo;
        this.groupRepo = groupRepo;
    }

    @PutMapping()
    public ResponseEntity<?> addList(@RequestParam Integer groupId, @RequestParam String name, HttpServletRequest request) {
        if (name.equals(""))
            return new ResponseEntity<>(new CustomResponse("Name cannot be empty"), HttpStatus.BAD_REQUEST);
        if(repo.findByNameInGroup(groupId, name).orElse(null) != null)
            return new ResponseEntity<>(new CustomResponse("List with that name already exists"), HttpStatus.CONFLICT);
        Integer clientId = CustomAuth.getUserId(request);
        if (clientId == null)
            return new ResponseEntity<>(new CustomResponse("Not logged in"), HttpStatus.UNAUTHORIZED);
        group = groupRepo.findGroupWithUsers(groupId).orElse(null);
        if(group == null)
            return new ResponseEntity<>(new CustomResponse("Group not found"), HttpStatus.NOT_FOUND);
        if(group.getUsers().stream().noneMatch(e -> e.getUser_id().equals(clientId)))
            return new ResponseEntity<>(new CustomResponse("Client not in list's group"), HttpStatus.FORBIDDEN);
        CheckList list = new CheckList(name, group);
        list = repo.save(list);
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @PatchMapping("{listId}")
    public ResponseEntity<?> changeListName(@PathVariable Integer listId, @RequestParam String name, HttpServletRequest request) {
        if(name.equals(""))
            return new ResponseEntity<>(new CustomResponse("Name cannot be empty"), HttpStatus.BAD_REQUEST);
        ResponseEntity<?> response = checkIfClientInGroup(listId, request);
        if(response != null)
            return response;
        if(repo.changeListName(listId, name) == 0)
            throw new EntityNotFoundException();
        return new ResponseEntity<>(new CustomResponse("List name changed"), HttpStatus.OK);
    }

    @DeleteMapping()
    public ResponseEntity<?> deleteList(@RequestParam Integer listId, HttpServletRequest request) {
        ResponseEntity<?> response = checkIfClientInGroup(listId, request);
        if(response != null)
            return response;
        repo.deleteById(listId);
        return new ResponseEntity<>(new CustomResponse("List deleted"), HttpStatus.OK);
    }

    @GetMapping()
    public ResponseEntity<?> getList(@RequestParam Integer listId, HttpServletRequest request) {
        ResponseEntity<?> response = checkIfClientInGroup(listId, request);
        if(response != null)
            return response;
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @GetMapping("{listId}/tasks")
    public ResponseEntity<?> getListTasks(@PathVariable Integer listId, HttpServletRequest request) {
        ResponseEntity<?> response = checkIfClientInGroup(listId, request);
        if(response != null)
            return response;
        return new ResponseEntity<>(repo.findListWithTasks(listId).orElseThrow().getTasks(), HttpStatus.OK);
    }

    @GetMapping("/group/{groupId}")
    public ResponseEntity<?> getListTasksByGroup(@PathVariable Integer groupId, @RequestParam String name, HttpServletRequest request) {
        CheckList list = repo.findListWithTasksByGroupAndName(groupId, name).orElse(null);
        if(list == null)
            return new ResponseEntity<>(new CustomResponse("List not found"), HttpStatus.NOT_FOUND);
        ResponseEntity<?> response = checkIfClientInGroup(list.getList_id(), request);
        if(response != null)
            return response;
        return new ResponseEntity<>(list.getTasks(), HttpStatus.OK);
    }

    private ResponseEntity<?> checkIfClientInGroup(Integer listId, HttpServletRequest request) {
        Integer clientId = CustomAuth.getUserId(request);
        if (clientId == null)
            return new ResponseEntity<>(new CustomResponse("Not logged in"), HttpStatus.UNAUTHORIZED);
        list = repo.findById(listId).orElse(null);
        if(list == null)
            return new ResponseEntity<>(new CustomResponse("List not found"), HttpStatus.NOT_FOUND);
        group = groupRepo.findGroupWithUsers(list.getGroup().getGroup_id()).orElseThrow();
        if(group.getUsers().stream().noneMatch(e -> e.getUser_id().equals(clientId)))
            return new ResponseEntity<>(new CustomResponse("Client not in list's group"), HttpStatus.FORBIDDEN);
        return null;
    }


}
