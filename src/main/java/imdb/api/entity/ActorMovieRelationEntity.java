package imdb.api.entity;

import lombok.*;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

@Entity
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@Builder
public class ActorMovieRelationEntity {

    @EmbeddedId
    private ActorMovieRelationId actorMovieRelationId;


}
