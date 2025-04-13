package am.shavigh.dbmigration.repository.postgres;

import am.shavigh.dbmigration.model.postgres.BibleTranslation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostgresRepo extends JpaRepository<BibleTranslation, Long> {
}
