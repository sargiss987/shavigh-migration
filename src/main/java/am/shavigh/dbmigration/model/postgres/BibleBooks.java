package am.shavigh.dbmigration.model.postgres;

import jakarta.persistence.*;
import java.util.List;

@Entity
public class BibleBooks {

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
    private Bibles bibles;

    @OneToMany(mappedBy = "bibleBooks", cascade = CascadeType.ALL, orphanRemoval = true)
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

    public Bibles getBible() {
        return bibles;
    }

    public void setBible(Bibles bibles) {
        this.bibles = bibles;
    }

    public List<BibleBookChapters> getBibleBookChapters() {
        return bibleBookChapters;
    }

    public void setBibleBookChapters(List<BibleBookChapters> bibleBookChapters) {
        this.bibleBookChapters = bibleBookChapters;
    }
}
