package pz.gr3.serwer.tables;

import com.fasterxml.jackson.annotation.*;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Groups")
public class Group {
    private Integer group_id;
    private String name;



    @Column(name ="user_id")
    private User owner;

    private List<User> users = new ArrayList<>();
    private List<CheckList> lists = new ArrayList<>();

    public Group() {}
    public Group(String name, User owner) {
        this.name = name;
        this.owner = owner;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public Integer getGroup_id() {
        return group_id;
    }
    public void setGroup_id(Integer group_id) {
        this.group_id = group_id;
    }

    @Column(nullable = false)
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"email", "creation_date", "last_login"})
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @ManyToOne(optional = false)
    public User getOwner() {
        return owner;
    }
    public void setOwner(User owner) {
        this.owner = owner;
    }

    @JsonIgnore
    @ArraySchema(schema = @Schema(implementation = Integer.class))
    @ManyToMany
    @JoinTable(
            name = "users_in_groups",
            joinColumns = @JoinColumn(name = "group_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    @JsonIgnore
    @ArraySchema(schema = @Schema(implementation = Integer.class))
    @OneToMany(mappedBy = "group", cascade = CascadeType.REMOVE)
    public List<CheckList> getLists() {
        return lists;
    }
    public void setLists(List<CheckList> lists) {
        this.lists = lists;
    }
}
