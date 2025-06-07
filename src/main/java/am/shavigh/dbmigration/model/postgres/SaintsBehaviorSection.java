package am.shavigh.dbmigration.model.postgres;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "saints_behavior_section")
public class SaintsBehaviorSection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String url;

    @Column(length = 20)
    private String status = "publish";

    @ManyToOne
    @JoinColumn(name = "saints_behavior_id", nullable = false)
    @JsonBackReference
    private SaintsBehavior saintsBehavior;

    @OneToMany(mappedBy = "saintsBehaviorSection", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<SaintsBehaviorSectionPage> pages = new ArrayList<>();

    // Getters and setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
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

    public SaintsBehavior getSaintsBehavior() {
        return saintsBehavior;
    }

    public void setSaintsBehavior(SaintsBehavior saintsBehavior) {
        this.saintsBehavior = saintsBehavior;
    }

    public List<SaintsBehaviorSectionPage> getPages() {
        return pages;
    }

    public void setPages(List<SaintsBehaviorSectionPage> pages) {
        this.pages = pages;
    }
}
