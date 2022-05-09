package pz.gr3.serwer.tables;

import com.fasterxml.jackson.annotation.*;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import org.apache.commons.validator.routines.EmailValidator;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Users")
public class User {
    private Integer user_id;
    private String name;
    private String email;
    private String password;
    private LocalDateTime creation_date;
    private LocalDateTime last_login;
    private static final String date_format = "yyyy-MM-dd HH:mm:ss";

    private List<Task> tasks = new ArrayList<>();
    private List<Group> groups = new ArrayList<>();
    private List<Group> owned_groups = new ArrayList<>();

    public User() {}
    public User(Integer user_id){ this.user_id = user_id;}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public Integer getUser_id() {
        return user_id;
    }
    public void setUser_id(Integer user_id) {
        this.user_id = user_id;
    }

    @Column(nullable = false)
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    @Column(unique = true, nullable = false)
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    @Hidden
    @Column(nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    public String getPassword() {
        return this.password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @JsonFormat(pattern = date_format)
    public LocalDateTime getCreation_date() {
        return creation_date;
    }
    public void setCreation_date(LocalDateTime creation_date) {
        this.creation_date = creation_date;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @JsonFormat(pattern = date_format)
    public LocalDateTime getLast_login() {
        return last_login;
    }
    public void setLast_login(LocalDateTime last_login) {
        this.last_login = last_login;
    }

    @JsonIgnore
    @ArraySchema(schema = @Schema(implementation = Integer.class))
    @OneToMany(mappedBy = "user")
    public List<Task> getTasks() {
        return tasks;
    }
    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

    @JsonIgnore
    @ArraySchema(schema = @Schema(implementation = Integer.class))
    @ManyToMany(mappedBy = "users")
    public List<Group> getGroups() {
        return groups;
    }
    public void setGroups(List<Group> groups) {
        this.groups = groups;
    }

    @JsonIgnore
    @ArraySchema(schema = @Schema(implementation = Integer.class))
    @OneToMany(mappedBy = "owner", cascade = CascadeType.REMOVE)
    public List<Group> getOwned_groups() {
        return owned_groups;
    }
    public void setOwned_groups(List<Group> owned_groups) {
        this.owned_groups = owned_groups;
    }

    @Transient
    @JsonIgnore
    public static boolean isValidEmail(String email) {
        return EmailValidator.getInstance(false).isValid(email);
    }
}
