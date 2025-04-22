package am.shavigh.dbmigration.repository.mysql;


import am.shavigh.dbmigration.model.mysql.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MysqlRepo extends JpaRepository<Post, Long> {

    @Query("SELECT p FROM Post p WHERE p.postName = :postName AND p.postType = 'post' AND p.postStatus = 'publish'")
    List<Post> findByPostName(@Param("postName") String postName);
}
