package am.shavigh.dbmigration.model.postgres;

import jakarta.persistence.*;

@Entity
public class BibleBookChapterPages {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String content;
    private String oldUniqueName;
    private String url;
    private String urlArmenian;
    private String nextLink;
    private String prevLink;
    private boolean hasNestedLinks;

    public String getUrlArmenian() {
        return urlArmenian;
    }

    public void setUrlArmenian(String urlArmenian) {
        this.urlArmenian = urlArmenian;
    }

    @ManyToOne
    @JoinColumn(name = "bible_book_chapter_id", nullable = false)
    private BibleBookChapters bibleBookChapters;

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


    public String getNextLink() {
        return nextLink;
    }

    public void setNextLink(String nextLink) {
        this.nextLink = nextLink;
    }

    public String getPrevLink() {
        return prevLink;
    }

    public void setPrevLink(String prevLink) {
        this.prevLink = prevLink;
    }

    public BibleBookChapters getBibleBookChapters() {
        return bibleBookChapters;
    }

    public void setBibleBookChapters(BibleBookChapters bibleBookChapters) {
        this.bibleBookChapters = bibleBookChapters;
    }

    public boolean isHasNestedLinks() {
        return hasNestedLinks;
    }

    public void setHasNestedLinks(boolean hasNestedLinks) {
        this.hasNestedLinks = hasNestedLinks;
    }
}
