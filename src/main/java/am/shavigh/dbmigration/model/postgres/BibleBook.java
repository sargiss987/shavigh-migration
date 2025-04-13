package am.shavigh.dbmigration.model.postgres;

import jakarta.persistence.*;
import java.util.List;

@Entity
public class BibleBook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private int serialNumber;

    @ManyToOne
    @JoinColumn(name = "translation_id", nullable = false)
    private BibleTranslation bibleTranslation;

    @ManyToOne
    @JoinColumn(name = "bible_id", nullable = false)
    private Bible bible;

    @OneToMany(mappedBy = "bibleBook", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BibleBookChapters> bibleBookChapters;

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

    public int getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(int serialNumber) {
        this.serialNumber = serialNumber;
    }

    public BibleTranslation getBibleTranslation() {
        return bibleTranslation;
    }

    public void setBibleTranslation(BibleTranslation bibleTranslation) {
        this.bibleTranslation = bibleTranslation;
    }

    public Bible getBible() {
        return bible;
    }

    public void setBible(Bible bible) {
        this.bible = bible;
    }

    public List<BibleBookChapters> getBibleBookChapters() {
        return bibleBookChapters;
    }

    public void setBibleBookChapters(List<BibleBookChapters> bibleBookChapters) {
        this.bibleBookChapters = bibleBookChapters;
    }
}
