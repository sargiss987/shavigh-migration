package am.shavigh.dbmigration.repository.postgres;

import am.shavigh.dbmigration.model.postgres.BibleBooks;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BibleBookRepo extends JpaRepository<BibleBooks, Long> {}
