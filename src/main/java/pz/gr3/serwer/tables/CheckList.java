package pz.gr3.serwer.tables;

import com.fasterxml.jackson.annotation.*;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Lists")
public class CheckList {
    private Integer list_id;
    @Column(name = "group_id")
    private Group group;
    private String name;

    private List<Task> tasks = new ArrayList<>();

    public CheckList() {}
    public CheckList(String name, Group group) {
        this.name = name;
        this.group = group;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public Integer getList_id() {
        return list_id;
    }
    public void setList_id(Integer list_id) {
        this.list_id = list_id;
    }

    @JoinColumn(name = "group_id", nullable = false)
    @JsonIgnoreProperties({"owner"})
    @ManyToOne(optional = false)
    public Group getGroup() {
        return group;
    }
    public void setGroup(Group group) {
        this.group = group;
    }

    @Column(nullable = false)
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    @JsonIgnore
    @ArraySchema(schema = @Schema(implementation = Integer.class))
    @OneToMany(mappedBy = "list", cascade = CascadeType.REMOVE)
    public List<Task> getTasks() {
        return tasks;
    }
    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }
}
