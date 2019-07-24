package imdb.api.entity;

import lombok.*;

import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
@Builder
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@Data
public class ActorMovieRelationId implements Serializable {

    private Long actorId;
    private Long movieId;


}
