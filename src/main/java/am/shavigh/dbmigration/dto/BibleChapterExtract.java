package am.shavigh.dbmigration.dto;

public class BibleChapterExtract {

    private String breadcrumbs;
    private String contentHtml;

    public BibleChapterExtract(String breadcrumbs, String contentHtml) {
        this.breadcrumbs = breadcrumbs;
        this.contentHtml = contentHtml;
    }

    public String getBreadcrumbs() {
        return breadcrumbs;
    }

    public void setBreadcrumbs(String breadcrumbs) {
        this.breadcrumbs = breadcrumbs;
    }

    public String getContentHtml() {
        return contentHtml;
    }

    public void setContentHtml(String contentHtml) {
        this.contentHtml = contentHtml;
    }
}
