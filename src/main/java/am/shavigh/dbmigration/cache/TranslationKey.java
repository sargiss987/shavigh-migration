package am.shavigh.dbmigration.cache;

import java.io.Serializable;
import java.util.Objects;

public class TranslationKey implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String text;
    private final String sourceLang;
    private final String targetLang;

    public TranslationKey(String text, String sourceLang, String targetLang) {
        this.text = text;
        this.sourceLang = sourceLang;
        this.targetLang = targetLang;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TranslationKey)) return false;
        TranslationKey that = (TranslationKey) o;
        return Objects.equals(text, that.text) &&
                Objects.equals(sourceLang, that.sourceLang) &&
                Objects.equals(targetLang, that.targetLang);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text, sourceLang, targetLang);
    }

    @Override
    public String toString() {
        return text + "::" + sourceLang + "->" + targetLang;
    }
}

