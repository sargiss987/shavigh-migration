package am.shavigh.dbmigration.service;

import am.shavigh.dbmigration.model.postgres.BibleBookChapters;
import am.shavigh.dbmigration.model.postgres.BibleBooks;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MigrationCheckpoint implements Serializable {
    private String postUrl;
    private int currentIteration;
    private List<BibleBookChapters> allChapters = new ArrayList<>();
    private Map<Long, BibleBooks> bibleBooksMap = new HashMap<>();
    private boolean booksSaved;
    private String lastProcessedPostUrl;

    public MigrationCheckpoint() {
    }

    public MigrationCheckpoint(String postUrl, int currentIteration, List<BibleBookChapters> allChapters, Map<Long, BibleBooks> bibleBooksMap, boolean booksSaved, String lastProcessedPostUrl) {
        this.postUrl = postUrl;
        this.currentIteration = currentIteration;
        this.allChapters = allChapters;
        this.bibleBooksMap = bibleBooksMap;
        this.booksSaved = booksSaved;
        this.lastProcessedPostUrl = lastProcessedPostUrl;
    }

    public String getPostUrl() {
        return postUrl;
    }

    public void setPostUrl(String postUrl) {
        this.postUrl = postUrl;
    }

    public int getCurrentIteration() {
        return currentIteration;
    }

    public void setCurrentIteration(int currentIteration) {
        this.currentIteration = currentIteration;
    }

    public List<BibleBookChapters> getAllChapters() {
        return allChapters;
    }

    public void setAllChapters(List<BibleBookChapters> allChapters) {
        this.allChapters = allChapters;
    }

    public Map<Long, BibleBooks> getBibleBooksMap() {
        return bibleBooksMap;
    }

    public void setBibleBooksMap(Map<Long, BibleBooks> bibleBooksMap) {
        this.bibleBooksMap = bibleBooksMap;
    }

    public boolean isBooksSaved() {
        return booksSaved;
    }

    public void setBooksSaved(boolean booksSaved) {
        this.booksSaved = booksSaved;
    }

    public String getLastProcessedPostUrl() {
        return lastProcessedPostUrl;
    }

    public void setLastProcessedPostUrl(String lastProcessedPostUrl) {
        this.lastProcessedPostUrl = lastProcessedPostUrl;
    }
}

