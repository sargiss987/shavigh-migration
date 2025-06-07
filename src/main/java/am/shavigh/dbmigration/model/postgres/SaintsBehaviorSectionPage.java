package am.shavigh.dbmigration.model.postgres;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

@Entity
@Table(name = "saints_behavior_section_page")
public class SaintsBehaviorSectionPage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String title;

    private String content;

    private String url;

    private String status = "publish";

    @ManyToOne
    @JoinColumn(name = "saints_behavior_section_id", nullable = false)
    @JsonBackReference
    private SaintsBehaviorSection saintsBehaviorSection;

    // Constructors
    public SaintsBehaviorSectionPage() {
    }

    public SaintsBehaviorSectionPage(Integer id, String title, String content, String url, String status, SaintsBehaviorSection saintsBehaviorSection) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.url = url;
        this.status = status;
        this.saintsBehaviorSection = saintsBehaviorSection;
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

    public SaintsBehaviorSection getSaintsBehaviorSection() {
        return saintsBehaviorSection;
    }

    public void setSaintsBehaviorSection(SaintsBehaviorSection saintsBehaviorSection) {
        this.saintsBehaviorSection = saintsBehaviorSection;
    }
}
