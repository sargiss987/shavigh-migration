package am.shavigh.dbmigration.repository.postgres;

import am.shavigh.dbmigration.model.postgres.SaintsBehavior;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SaintsBehaviorRepo extends JpaRepository<SaintsBehavior, Long> {}
