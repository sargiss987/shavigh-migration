package am.shavigh.dbmigration.repository.postgres;

import am.shavigh.dbmigration.model.postgres.Bibles;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BibleRepo extends JpaRepository<Bibles, Long> {
}
