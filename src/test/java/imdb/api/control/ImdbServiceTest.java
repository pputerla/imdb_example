package imdb.api.control;

import imdb.api.entity.ActorEntity;
import imdb.api.entity.MovieEntity;
import io.swagger.model.Actor;
import io.swagger.model.Appearance;
import io.swagger.model.Movie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class ImdbServiceTest {

    private static final String SOME_NAME = "some name";
    private static final Integer SOME_YEAR = 3323;
    private static final String SOME_TITLE = "some title";
    private static final long TOTAL_ENTRIES = 2222;
    private static final Long SOME_ID = 234234L;
    @Mock
    private MovieRepository movieRepository;
    @Mock
    private ActorRepository actorRepository;
    @Mock
    private ActorMovieRelationRepository actorMovieRelationRepository;
    @Captor
    private ArgumentCaptor<Pageable> pageableArgumentCaptor;

    private ImdbService sut;

    @BeforeEach
    void init() {
        sut = new ImdbService(movieRepository, actorRepository, actorMovieRelationRepository);
    }

    @ParameterizedTest
    @CsvSource({"1,10", "222,10", "1,1", "222,1"})
    void findMovies(String pageString, String pageSizeString) {
        MovieEntity movieEntity = MovieEntity
                .builder()
                .id(SOME_ID)
                .title(SOME_TITLE)
                .year(SOME_YEAR)
                .build();
        BigDecimal page = new BigDecimal(pageString);
        BigDecimal pageSize = new BigDecimal(pageSizeString);
        PageRequest pageRequest = PageRequest.of(page.intValue(), pageSize.intValue());
        Page<MovieEntity> moviePage = new PageImpl<>(Collections.singletonList(movieEntity), pageRequest, TOTAL_ENTRIES);
        when(movieRepository.findAll(any(Pageable.class))).thenReturn(moviePage);

        //when
        List<Movie> result = sut.findMovies(page, pageSize, null);

        //then
        verify(movieRepository).findAll(pageableArgumentCaptor.capture());
        assertEquals(page.intValue(), pageableArgumentCaptor.getValue().getPageNumber());
        assertEquals(pageSize.intValue(), pageableArgumentCaptor.getValue().getPageSize());
        assertEquals(1, result.size());
        assertEquals(SOME_TITLE, result.get(0).getTitle());
        assertEquals("" + SOME_YEAR, result.get(0).getYear());
        assertEquals(BigDecimal.valueOf(SOME_ID), result.get(0).getId());


    }

    @Test
    void shouldFindMovie() {
        //given
        MovieEntity movieEntity = MovieEntity
                .builder()
                .id(SOME_ID)
                .title(SOME_TITLE)
                .year(SOME_YEAR)
                .build();
        when(movieRepository.findById(SOME_ID)).thenReturn(Optional.of(movieEntity));


        //when
        Optional<Movie> movie = sut.findMovie(BigDecimal.valueOf(SOME_ID));

        //then
        verify(movieRepository).findById(SOME_ID);
        assertTrue(movie.isPresent());
        assertEquals(SOME_TITLE, movie.get().getTitle());
        assertEquals("" + SOME_YEAR, movie.get().getYear());
        assertEquals(BigDecimal.valueOf(SOME_ID), movie.get().getId());
    }

    @Test
    void shouldNotFindMovie() {
        //given
        when(movieRepository.findById(SOME_ID)).thenReturn(Optional.empty());


        //when
        Optional<Movie> movie = sut.findMovie(BigDecimal.valueOf(SOME_ID));

        //then
        verify(movieRepository).findById(SOME_ID);
        assertFalse(movie.isPresent());
    }

    @Test
    void shouldFindActor() {
        //given
        ActorEntity actorEntity = ActorEntity
                .builder()
                .id(SOME_ID)
                .name(SOME_NAME)
                .movieIds(Collections.emptyList())
                .build();
        when(actorRepository.findById(SOME_ID)).thenReturn(Optional.of(actorEntity));


        //when
        Optional<Actor> actor = sut.findActor(BigDecimal.valueOf(SOME_ID));

        //then
        verify(actorRepository).findById(SOME_ID);
        assertTrue(actor.isPresent());
        assertEquals(SOME_NAME, actor.get().getName());
        assertEquals(BigDecimal.valueOf(SOME_ID), actor.get().getId());
    }

    @Test
    void shouldNotFindActor() {
        //given
        when(actorRepository.findById(SOME_ID)).thenReturn(Optional.empty());


        //when
        Optional<Actor> actor = sut.findActor(BigDecimal.valueOf(SOME_ID));

        //then
        verify(actorRepository).findById(SOME_ID);
        assertFalse(actor.isPresent());
    }


    @ParameterizedTest
    @CsvSource({"1,10", "222,10", "1,1", "222,1"})
    void findActors(String pageString, String pageSizeString) {
        ActorEntity actorEntity = ActorEntity
                .builder()
                .id(SOME_ID)
                .name(SOME_NAME)
                .movieIds(Collections.emptyList())
                .build();
        BigDecimal page = new BigDecimal(pageString);
        BigDecimal pageSize = new BigDecimal(pageSizeString);
        PageRequest pageRequest = PageRequest.of(page.intValue(), pageSize.intValue());
        Page<ActorEntity> actorPage = new PageImpl<>(Collections.singletonList(actorEntity), pageRequest, TOTAL_ENTRIES);
        when(actorRepository.findAll(any(Pageable.class))).thenReturn(actorPage);

        //when
        List<Actor> result = sut.findActors(page, pageSize, null);

        //then
        verify(actorRepository).findAll(pageableArgumentCaptor.capture());
        assertEquals(page.intValue(), pageableArgumentCaptor.getValue().getPageNumber());
        assertEquals(pageSize.intValue(), pageableArgumentCaptor.getValue().getPageSize());
        assertEquals(1, result.size());
        assertEquals(SOME_NAME, result.get(0).getName());
        assertEquals(BigDecimal.valueOf(SOME_ID), result.get(0).getId());
    }


    @ParameterizedTest
    @CsvSource({"1,10", "222,10", "1,1", "222,1"})
    void shouldNotFindAppearances(String pageString, String pageSizeString) {
        //given
        BigDecimal page = new BigDecimal(pageString);
        BigDecimal pageSize = new BigDecimal(pageSizeString);

        //when
        List<Appearance> result = sut.findAppearance(BigDecimal.valueOf(SOME_ID), page, pageSize);

        //then
        verify(actorRepository).findById(SOME_ID);
        verify(movieRepository, never()).findAllById(anyIterable());
        assertEquals(0, result.size());
    }

    //no test for shouldFindAppearances
}