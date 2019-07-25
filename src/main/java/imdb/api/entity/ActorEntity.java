package imdb.api.entity;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.util.List;

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
