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
@RequestMapping("groups")
public class GroupController {
    private Group group;
    private final GroupRepository repo;
    private final UserRepository userRepo;

    public GroupController(GroupRepository repo, UserRepository userRepo) {
        this.repo = repo;
        this.userRepo = userRepo;
    }

    @PutMapping()
    public ResponseEntity<?> addGroup(@RequestParam String name, HttpServletRequest request) {
        if (name.equals(""))
            return new ResponseEntity<>(new CustomResponse("Name cannot be empty"), HttpStatus.BAD_REQUEST);
        Integer clientId = CustomAuth.getUserId(request);
        if (clientId == null)
            return new ResponseEntity<>(new CustomResponse("Not logged in"), HttpStatus.UNAUTHORIZED);
        User user = userRepo.findById(clientId).orElseThrow();
        Group group = new Group(name, user);
        group.getUsers().add(user);
        group = repo.save(group);
        return new ResponseEntity<>(group, HttpStatus.OK);
    }

    @PatchMapping("{groupId}")
    public ResponseEntity<?> updateGroupName(@PathVariable Integer groupId, @RequestParam String name, HttpServletRequest request) {
        if(name.equals(""))
            return new ResponseEntity<>(new CustomResponse("Name cannot be empty"), HttpStatus.BAD_REQUEST);
        ResponseEntity<?> response = checkIfOwner(groupId, request);
        if(response != null)
            return response;
        if(repo.changeGroupName(groupId, name) == 0)
            throw new EntityNotFoundException();
        return new ResponseEntity<>(new CustomResponse("Group name changed"), HttpStatus.OK);
    }

    @DeleteMapping()
    public ResponseEntity<?> deleteGroup(@RequestParam Integer groupId, HttpServletRequest request) {
        ResponseEntity<?> response = checkIfOwner(groupId, request);
        if(response != null)
            return response;
        repo.deleteById(groupId);
        return new ResponseEntity<>(new CustomResponse("Group deleted"), HttpStatus.OK);
    }

    private ResponseEntity<?> checkIfOwner(Integer groupId, HttpServletRequest request) {
        Integer clientId = CustomAuth.getUserId(request);
        if (clientId == null)
            return new ResponseEntity<>(new CustomResponse("Not logged in"), HttpStatus.UNAUTHORIZED);
        group = repo.findById(groupId).orElse(null);
        if(group == null)
            return new ResponseEntity<>(new CustomResponse("Group not found"), HttpStatus.NOT_FOUND);
        if (!clientId.equals(group.getOwner().getUser_id()))
            return new ResponseEntity<>(new CustomResponse("You are not the owner"), HttpStatus.FORBIDDEN);
        return null;
    }

    @PostMapping("{groupId}")
    public ResponseEntity<?> addUserToGroup(@PathVariable Integer groupId, @RequestParam(required = false) String email , HttpServletRequest request) {
        return changeMembershipState("add", groupId, email, request);
    }

    @DeleteMapping("{groupId}")
    public ResponseEntity<?> deleteUserFromGroup(@PathVariable Integer groupId, @RequestParam(required = false) String email, HttpServletRequest request) {
        return changeMembershipState("delete", groupId, email, request);
    }

    private ResponseEntity<?> changeMembershipState(String type, Integer groupId, String email, HttpServletRequest request) {
        Integer clientId = CustomAuth.getUserId(request);
        if (clientId == null)
            return new ResponseEntity<>(new CustomResponse("Not logged in"), HttpStatus.UNAUTHORIZED);
        if(!User.isValidEmail(email)) {
            return new ResponseEntity<>(new CustomResponse("Email not in valid format"), HttpStatus.BAD_REQUEST);
        }
        User user;
        if(email == null)
            user = userRepo.findById(clientId).orElseThrow();
        else
            user = userRepo.findByEmail(email).orElse(null);
        if(user == null)
            return new ResponseEntity<>(new CustomResponse("User does not exist"), HttpStatus.NOT_FOUND);
        ResponseEntity<?> response = checkIfClientInGroup(groupId, clientId);
        if(response != null)
            return response;
        if(group.getUsers().stream().anyMatch(e -> e.getUser_id().equals(user.getUser_id()))) {
            if (type.equals("add"))
                return new ResponseEntity<>(new CustomResponse("User already in group"), HttpStatus.CONFLICT);
        }
        else if (type.equals("delete"))
            return new ResponseEntity<>(new CustomResponse("User not in group"), HttpStatus.CONFLICT);
        switch (type) {
                case "add":
                    group.getUsers().add(user);
                    repo.save(group);
                    return new ResponseEntity<>(user, HttpStatus.OK);
                case "delete":
                    if(user.getUser_id().equals(group.getOwner().getUser_id()))
                        return new ResponseEntity<>(new CustomResponse("Owner cannot leave group"), HttpStatus.CONFLICT);
                    repo.deleteUserFromGroup(user.getUser_id());
                    return new ResponseEntity<>(new CustomResponse("User deleted from group"), HttpStatus.OK);
        }
        throw new NullPointerException();
    }

    @GetMapping()
    public ResponseEntity<?> getGroup(@RequestParam Integer groupId, HttpServletRequest request) {
        return getRelations("group", groupId, request);
    }

    @GetMapping("{groupId}/users")
    public ResponseEntity<?> getGroupUsers(@PathVariable Integer groupId, HttpServletRequest request) {
        return getRelations("users", groupId, request);
    }

    @GetMapping("{groupId}/lists")
    public ResponseEntity<?> getGroupLists(@PathVariable Integer groupId, HttpServletRequest request) {
        return getRelations("lists", groupId, request);
    }

    private ResponseEntity<?> getRelations(String type, Integer groupId, HttpServletRequest request) {
        Integer clientId = CustomAuth.getUserId(request);
        if (clientId == null)
            return new ResponseEntity<>(new CustomResponse("Not logged in"), HttpStatus.UNAUTHORIZED);
        ResponseEntity<?> response = checkIfClientInGroup(groupId, clientId);
        if(response != null)
            return response;
        switch (type) {
            case "group":
                return new ResponseEntity<>(group, HttpStatus.OK);
            case "users":
                return new ResponseEntity<>(group.getUsers(), HttpStatus.OK);
            case "lists":
                return new ResponseEntity<>(repo.findGroupWithLists(groupId).orElseThrow().getLists(), HttpStatus.OK);
        }
        throw new NullPointerException();
    }

    private ResponseEntity<?> checkIfClientInGroup(Integer groupId, Integer clientId) {
        group = repo.findGroupWithUsers(groupId).orElse(null);
        if(group == null)
            return new ResponseEntity<>(new CustomResponse("Group not found"), HttpStatus.NOT_FOUND);
        if(group.getUsers().stream().noneMatch(e -> e.getUser_id().equals(clientId)))
            return new ResponseEntity<>(new CustomResponse("Client not in group"), HttpStatus.FORBIDDEN);
        return null;
    }
}