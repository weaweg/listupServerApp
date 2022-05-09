package pz.gr3.serwer.tables;

import com.fasterxml.jackson.annotation.*;

import javax.persistence.*;

@Entity
@Table(name = "Tasks")
public class Task {
    private Integer task_id;
    @Column(name = "list_id")
    private CheckList list;
    @Column(name = "user_id")
    private User user;
    private String description;
    private boolean status;

    public Task(){}
    public Task(String description, CheckList list, boolean status) {
        this.description = description;
        this.list = list;
        this.status = status;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public Integer getTask_id() {
        return task_id;
    }

    public void setTask_id(Integer task_id) {
        this.task_id = task_id;
    }

    @JoinColumn(name = "list_id", nullable = false)
    @JsonIgnoreProperties({"tasks"})
    @ManyToOne(optional = false)
    public CheckList getList() {
        return list;
    }

    public void setList(CheckList list) {
        this.list = list;
    }

    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties({"email", "creation_date", "last_login"})
    @ManyToOne
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Column(nullable = false)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Column(nullable = false)
    public boolean getStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }
}
