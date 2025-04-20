package am.shavigh.dbmigration.model.postgres;

import jakarta.persistence.*;

@Entity
public class BibleBookChapters {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String content;

    // TO:DO supposed to be removed
    private String oldUniqueName;

    private String url;

    @ManyToOne
    @JoinColumn(name = "bible_book_id", nullable = false)
    private BibleBooks bibleBooks;

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

    public String getOldUniqueName() {
        return oldUniqueName;
    }

    public void setOldUniqueName(String oldUniqueName) {
        this.oldUniqueName = oldUniqueName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public BibleBooks getBibleBook() {
        return bibleBooks;
    }

    public void setBibleBook(BibleBooks bibleBooks) {
        this.bibleBooks = bibleBooks;
    }
}
