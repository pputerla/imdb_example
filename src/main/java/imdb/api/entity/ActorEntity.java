package imdb.api.entity;

import io.swagger.model.Movie;
import lombok.*;

import javax.persistence.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@Builder
public class ActorEntity {

    @Id
    @EqualsAndHashCode.Include
    private Long id;

    @Column(length = 4096)
    private String name;


    @Transient
    private List<Long> movieIds;


}
