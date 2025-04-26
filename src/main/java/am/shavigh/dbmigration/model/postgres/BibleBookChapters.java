package am.shavigh.dbmigration.model.postgres;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.List;

@Entity
public class BibleBookChapters implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String content;

    // TO:DO supposed to be removed
    private String oldUniqueName;

    private String url;
    private String urlArmenian;

    private String linkToDefaultContent;

    private String nextLink;
    private String prevLink;

    private boolean unexpectedLink;

    public boolean isUnexpectedLink() {
        return unexpectedLink;
    }

    public void setUnexpectedLink(boolean unexpectedLink) {
        this.unexpectedLink = unexpectedLink;
    }

    public String getUrlArmenian() {
        return urlArmenian;
    }

    public void setUrlArmenian(String urlArmenian) {
        this.urlArmenian = urlArmenian;
    }

    public List<BibleBookChapterPages> getBibleBookChapterPages() {
        return bibleBookChapterPages;
    }

    public void setBibleBookChapterPages(List<BibleBookChapterPages> bibleBookChapterPages) {
        this.bibleBookChapterPages = bibleBookChapterPages;
    }

    @ManyToOne
    @JoinColumn(name = "bible_book_id", nullable = false)
    private BibleBooks bibleBooks;

    @OneToMany(mappedBy = "bibleBookChapters", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BibleBookChapterPages> bibleBookChapterPages;

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

    public String getLinkToDefaultContent() {
        return linkToDefaultContent;
    }

    public void setLinkToDefaultContent(String linkToDefaultContent) {
        this.linkToDefaultContent = linkToDefaultContent;
    }

    public BibleBooks getBibleBooks() {
        return bibleBooks;
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

    public void setBibleBooks(BibleBooks bibleBooks) {
        this.bibleBooks = bibleBooks;
    }
}
