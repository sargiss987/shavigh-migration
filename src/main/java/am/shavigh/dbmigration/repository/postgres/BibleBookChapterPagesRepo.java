package am.shavigh.dbmigration.repository.postgres;

import am.shavigh.dbmigration.model.postgres.BibleBookChapterPages;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BibleBookChapterPagesRepo extends JpaRepository<BibleBookChapterPages, Long> {

    List<BibleBookChapterPages> findAllByOldUniqueName(String oldUniqueName);
    List<BibleBookChapterPages> findByHasNestedLinksTrue();
}
