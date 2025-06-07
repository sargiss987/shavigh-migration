package am.shavigh.dbmigration.model.postgres;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "saints_behavior")
public class SaintsBehavior {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String title;

    private String url;

    private String status = "publish";

    @OneToMany(mappedBy = "saintsBehavior", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<SaintsBehaviorSection> sections = new ArrayList<>();

    public SaintsBehavior() {
    }

    public SaintsBehavior(Integer id, String title, String url, String status) {
        this.id = id;
        this.title = title;
        this.url = url;
        this.status = status;
    }

    // Getters and setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<SaintsBehaviorSection> getSections() {
        return sections;
    }

    public void setSections(List<SaintsBehaviorSection> sections) {
        this.sections = sections;
    }

}
