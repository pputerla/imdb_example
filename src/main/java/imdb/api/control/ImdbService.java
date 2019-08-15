package imdb.api.control;

import imdb.api.entity.ActorEntity;
import imdb.api.entity.ActorMovieRelationEntity;
import imdb.api.entity.ActorMovieRelationId;
import imdb.api.entity.MovieEntity;
import io.swagger.model.Actor;
import io.swagger.model.Appearance;
import io.swagger.model.Movie;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ImdbService {

    private final MovieRepository movieRepository;
    private final ActorRepository actorRepository;
    private final ActorMovieRelationRepository actorMovieRelationRepository;

    private final Function<MovieEntity, Movie> movieMapper = movieEntity -> {
        Movie movie = new Movie();
        movie.setId(BigDecimal.valueOf(movieEntity.getId()));
        movie.setTitle(movieEntity.getTitle());
        movie.setYear(Optional.ofNullable(movieEntity.getYear()).map(Objects::toString).orElse(null));
        return movie;
    };

    private final Function<ActorEntity, Actor> actorMapper = actorEntity -> {
        Actor actor = new Actor();
        actor.setId(BigDecimal.valueOf(actorEntity.getId()));
        actor.setName(actorEntity.getName());
        return actor;
    };


    public List<Movie> findMovies(BigDecimal page, BigDecimal pageSize, String name) {
        return findMoviesPage(PageRequest.of(page.intValue(), pageSize.intValue(), Sort.by("id")), name)
                .map(movieMapper)
                .getContent();

    }

    public Optional<Movie> findMovie(BigDecimal id) {
        return movieRepository
                .findById(id.longValue())
                .map(movieMapper);
    }

    public List<Actor> findActors(BigDecimal page, BigDecimal pageSize, String name) {
        return findActorsPage(PageRequest.of(page.intValue(), pageSize.intValue(), Sort.by("id")), name)
                .map(actorMapper)
                .getContent();
    }

    private Page<ActorEntity> findActorsPage(Pageable page, String name) {
        if (name == null || name.isBlank()) {
            return actorRepository.findAll(page);
        }
        return actorRepository.findByNameIgnoreCaseContaining(name, page);
    }

    private Page<MovieEntity> findMoviesPage(Pageable page, String name) {
        if (name == null || name.isBlank()) {
            return movieRepository.findAll(page);
        }
        return movieRepository.findByTitleIgnoreCaseContaining(name, page);
    }

    public Optional<Actor> findActor(BigDecimal id) {
        return actorRepository
                .findById(id.longValue())
                .map(actorMapper);
    }

    public List<Appearance> findAppearance(BigDecimal actorId) {
        return actorRepository
                .findById(actorId.longValue())
                .map(actor -> {
                    List<Long> movieIds = actorMovieRelationRepository
                            .findByActorId(actor.getId())
                            .stream()
                            .map(ActorMovieRelationEntity::getActorMovieRelationId)
                            .map(ActorMovieRelationId::getMovieId)
                            .collect(Collectors.toList());

                    return movieRepository
                            .findAllById(movieIds)
                            .stream()
                            .map(movie -> {
                                Appearance appearance = new Appearance();
                                appearance.setCharacterName(actor.getName());
                                appearance.setMovieId(BigDecimal.valueOf(movie.getId()));
                                appearance.setMovieName(movie.getTitle());
                                return appearance;
                            })
                            .collect(Collectors.toList());
                })
                .orElse(Collections.emptyList());
    }
}
