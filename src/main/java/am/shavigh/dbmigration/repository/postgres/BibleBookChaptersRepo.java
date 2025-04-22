package am.shavigh.dbmigration.repository.postgres;

import am.shavigh.dbmigration.model.postgres.BibleBookChapters;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BibleBookChaptersRepo extends JpaRepository<BibleBookChapters, Long> {}
