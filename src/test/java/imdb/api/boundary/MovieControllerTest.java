package imdb.api.boundary;

import imdb.api.control.ImdbService;
import io.swagger.model.Movie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MovieControllerTest {

    private static final String SOME_NAME = "someName";

    @Mock
    private ImdbService imdbService;

    @Mock
    private List<Movie> movies;

    private MovieController sut;

    @BeforeEach
    void init() {
        sut = new MovieController(imdbService);
    }

    @Test
    void shouldGetMovies() {
        //given
        BigDecimal page = mock(BigDecimal.class);
        BigDecimal pageSize = mock(BigDecimal.class);
        when(imdbService.findMovies(page, pageSize, SOME_NAME)).thenReturn(movies);

        //when
        ResponseEntity<List<Movie>> result = sut.moviesGet(page, pageSize, SOME_NAME);

        //then
        verify(imdbService).findMovies(page, pageSize, SOME_NAME);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(movies, result.getBody());
    }

    @Test
    void shouldGetMovie() {
        BigDecimal id = mock(BigDecimal.class);
        Movie movie = mock(Movie.class);
        when(imdbService.findMovie(id)).thenReturn(Optional.of(movie));

        //when
        ResponseEntity<Movie> result = sut.moviesIdGet(id);

        //then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(movie, result.getBody());
    }


    @Test
    void shouldNotGetMovie() {
        //given
        BigDecimal id = mock(BigDecimal.class);
        when(imdbService.findMovie(id)).thenReturn(Optional.empty());

        //when
        ResponseEntity<Movie> result = sut.moviesIdGet(id);

        //then
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertNull(result.getBody());
    }
}