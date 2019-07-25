package imdb.api.control;

import imdb.api.entity.ActorMovieRelationEntity;
import imdb.api.entity.ActorMovieRelationId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActorMovieRelationRepository extends JpaRepository<ActorMovieRelationEntity, ActorMovieRelationId> {

    @Query("select ar from ActorMovieRelationEntity ar where ar.actorMovieRelationId.actorId=:actorId")
    List<ActorMovieRelationEntity> findByActorId(@Param("actorId") Long actorId);
}
