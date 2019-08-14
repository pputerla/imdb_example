package imdb.api.control;

import imdb.api.entity.ActorEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActorRepository extends JpaRepository<ActorEntity, Long> {

    Page<ActorEntity> findByNameIgnoreCaseContaining(String name, Pageable pageable);
}
