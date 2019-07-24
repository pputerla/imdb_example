package imdb.api.entity;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class MovieEntity {

    @Id
    @EqualsAndHashCode.Include
    private Long id;
    @Column(length = 4096)
    private String title;
    private Integer year;

}
