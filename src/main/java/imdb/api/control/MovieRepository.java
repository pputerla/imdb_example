package imdb.api.control;

import imdb.api.entity.MovieEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MovieRepository extends JpaRepository<MovieEntity, Long> {

    Page<MovieEntity> findByTitleIgnoreCaseContaining(String title, Pageable pageable);

}
