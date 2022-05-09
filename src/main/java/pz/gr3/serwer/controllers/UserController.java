package pz.gr3.serwer.controllers;

import pz.gr3.serwer.repositories.*;
import pz.gr3.serwer.tables.*;
import pz.gr3.serwer.CustomResponse;
import pz.gr3.serwer.CustomAuth;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.ZoneOffset;


@RestController
@RequestMapping("users")
public class UserController {
    private final UserRepository repo;
    private final BCryptPasswordEncoder passEncoder = new BCryptPasswordEncoder();

    public UserController(UserRepository repo) {
        this.repo = repo;
    }

    @PutMapping("register")
    public ResponseEntity<?> registerNewUser(@RequestBody User cred) {
        if (repo.findByEmail(cred.getEmail()).isPresent())
            return new ResponseEntity<>(new CustomResponse("Email taken"), HttpStatus.CONFLICT);
        if (cred.getName() == null || cred.getName().equals(""))
            return new ResponseEntity<>(new CustomResponse("Name cannot be empty"), HttpStatus.BAD_REQUEST);
        if (!User.isValidEmail(cred.getEmail()))
            return new ResponseEntity<>(new CustomResponse("Not valid email"), HttpStatus.BAD_REQUEST);
        if (cred.getPassword() == null || cred.getPassword().equals(""))
            return new ResponseEntity<>(new CustomResponse("Password cannot be empty"), HttpStatus.BAD_REQUEST);

        cred.setCreation_date(LocalDateTime.now(ZoneOffset.UTC));
        cred.setPassword(passEncoder.encode(cred.getPassword()));
        repo.save(cred);
        return new ResponseEntity<>(new CustomResponse("User registered"), HttpStatus.CREATED);
    }

    @PostMapping("login")
    public ResponseEntity<?> logIn(@RequestBody User cred, HttpServletRequest request) {
        User user = repo.findByEmail(cred.getEmail()).orElse(null);
        if (user == null || !passEncoder.matches(cred.getPassword(), user.getPassword()))
            return new ResponseEntity<>(new CustomResponse("Not valid credentials"), HttpStatus.I_AM_A_TEAPOT);
        request.getSession().setAttribute("USER_ID", user.getUser_id());
        LocalDateTime tmp = user.getLast_login();
        user.setLast_login(LocalDateTime.now(ZoneOffset.UTC));
        repo.save(user);
        user.setLast_login(tmp);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @PostMapping("logout")
    public ResponseEntity<?> logOut(HttpServletRequest request) {
        Integer clientId = CustomAuth.getUserId(request);
        if (clientId == null)
            return new ResponseEntity<>(new CustomResponse("Not logged in"), HttpStatus.UNAUTHORIZED);
        request.getSession().invalidate();
        return new ResponseEntity<>(new CustomResponse("Logged out"), HttpStatus.OK);
    }

    @PatchMapping("update")
    public ResponseEntity<?> updateLoggedUser(@RequestBody User cred, HttpServletRequest request) {
        Integer clientId = CustomAuth.getUserId(request);
        if (clientId == null)
            return new ResponseEntity<>(new CustomResponse("Not logged in"), HttpStatus.UNAUTHORIZED);
        User client = repo.findById(clientId).orElseThrow();
        if (cred.getName() != null)
            client.setName(cred.getName());
        if (User.isValidEmail(cred.getEmail()))
            client.setEmail(cred.getEmail());
        if (cred.getPassword() != null)
            client.setPassword(passEncoder.encode(cred.getPassword()));
        repo.save(client);
        return new ResponseEntity<>(new CustomResponse("Changes saved"), HttpStatus.OK);
    }

    @DeleteMapping("self")
    public ResponseEntity<?> deleteLoggedUser(HttpServletRequest request) {
        Integer clientId = CustomAuth.getUserId(request);
        if (clientId == null)
            return new ResponseEntity<>(new CustomResponse("Not logged in"), HttpStatus.UNAUTHORIZED);
        repo.deleteById(clientId);
        request.getSession().invalidate();
        return new ResponseEntity<>(new CustomResponse("Account deleted"), HttpStatus.OK);
    }

    @GetMapping("self")
    public ResponseEntity<?> getLoggedUserData(HttpServletRequest request) {
        Integer clientId = CustomAuth.getUserId(request);
        if (clientId == null)
            return new ResponseEntity<>(new CustomResponse("Not logged in"), HttpStatus.UNAUTHORIZED);
        return new ResponseEntity<>(repo.findById(clientId).orElseThrow(), HttpStatus.OK);
    }
    @GetMapping()
    public ResponseEntity<?> findUser(@RequestParam(required = false) Integer userId,
                                      @RequestParam(required = false) String email, HttpServletRequest request) {
        Integer clientId = CustomAuth.getUserId(request);
        if (clientId == null)
            return new ResponseEntity<>(new CustomResponse("Not logged in"), HttpStatus.UNAUTHORIZED);
        if(email != null && !User.isValidEmail(email))
            return returnUser(repo.findByEmail(email).orElse(null));
        if(userId != null)
            return returnUser(repo.findById(userId).orElse(null));
        return new ResponseEntity<>(new CustomResponse("Not valid params"), HttpStatus.BAD_REQUEST);
    }

    private ResponseEntity<?> returnUser(User user) {
        if (user == null)
            return new ResponseEntity<>(new CustomResponse("User not found"), HttpStatus.NOT_FOUND);
        user.setEmail("********");
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @GetMapping("groups")
    public ResponseEntity<?> getGroups(HttpServletRequest request) {
        return getRelations("groups", request);
    }


    @GetMapping("tasks")
    public ResponseEntity<?> getTasks(HttpServletRequest request) {
        return getRelations("tasks", request);
    }

    @GetMapping("owned")
    public ResponseEntity<?> getOwnedGroups(HttpServletRequest request) {
        return getRelations("owned", request);
    }

    private ResponseEntity<?> getRelations(String type, HttpServletRequest request) {
        Integer clientId = CustomAuth.getUserId(request);
        if (clientId == null)
            return new ResponseEntity<>(new CustomResponse("Not logged in"), HttpStatus.UNAUTHORIZED);
        switch (type) {
            case "groups":
                return new ResponseEntity<>(repo.findUserWithGroups(clientId).orElseThrow().getGroups(), HttpStatus.OK);
            case "tasks":
                return new ResponseEntity<>(repo.findUserWithTasks(clientId).orElseThrow().getTasks(), HttpStatus.OK);
            case "owned":
                return new ResponseEntity<>(repo.findUserWithOwned(clientId).orElseThrow().getOwned_groups(), HttpStatus.OK);
            default:
                throw new NullPointerException();
        }
    }
}