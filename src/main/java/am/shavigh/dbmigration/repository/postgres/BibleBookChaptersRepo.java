package am.shavigh.dbmigration.repository.postgres;

import am.shavigh.dbmigration.model.postgres.BibleBookChapters;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BibleBookChaptersRepo extends JpaRepository<BibleBookChapters, Long> {

    @Query(value = """
            SELECT c.*
            FROM public.bible_book_chapters c
            JOIN public.bible_books b ON b.id = c.bible_book_id
            JOIN public.bible_translations t ON t.id = b.translation_id
            WHERE c.url_armenian ILIKE CONCAT('%', :segment1, '%')
              AND c.url_armenian ILIKE CONCAT('%', :segment2)
              AND t.name = :translationName
            """,
            nativeQuery = true)
    List<BibleBookChapters> findByUrlArmenianContainingTwoSegmentsAndTranslation(
            @Param("segment1") String segment1,   // e.g. "հինկտակարան/1."
            @Param("segment2") String segment2,   // e.g. "գլուխ1"
            @Param("translationName") String translationName // e.g. "Արարատ"
    );
}
