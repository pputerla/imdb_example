package imdb.api.boundary;

import imdb.api.control.ImdbService;
import io.swagger.model.Actor;
import io.swagger.model.Appearance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActorControllerTest {


    private static final String SOME_NAME = "someName";
    @Mock
    private ImdbService imdbService;

    @Mock
    private List<Actor> actors;

    @Mock
    private List<Appearance> appearances;

    private ActorController sut;

    @BeforeEach
    void init() {
        sut = new ActorController(imdbService);
    }

    @Test
    void shouldGetActors() {
        //given
        BigDecimal page = mock(BigDecimal.class);
        BigDecimal pageSize = mock(BigDecimal.class);
        when(imdbService.findActors(page, pageSize, SOME_NAME)).thenReturn(actors);

        //when
        ResponseEntity<List<Actor>> result = sut.actorsGet(page, pageSize, SOME_NAME);

        //then
        verify(imdbService).findActors(page, pageSize, SOME_NAME);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(actors, result.getBody());
    }

    @Test
    void shouldGetAppearances() {
        //given
        BigDecimal page = mock(BigDecimal.class);
        BigDecimal pageSize = mock(BigDecimal.class);
        BigDecimal actorId = mock(BigDecimal.class);
        when(imdbService.findAppearance(actorId, page, pageSize)).thenReturn(appearances);

        //when
        ResponseEntity<List<Appearance>> result = sut.actorsIdAppearancesGet(actorId, page, pageSize);

        //then
        verify(imdbService).findAppearance(actorId, page, pageSize);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(appearances, result.getBody());
    }

    @Test
    void shouldGetActor() {
        //given
        BigDecimal id = mock(BigDecimal.class);
        Actor actor = mock(Actor.class);
        when(imdbService.findActor(id)).thenReturn(Optional.of(actor));

        //when
        ResponseEntity<Actor> result = sut.actorsIdGet(id);

        //then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(actor,result.getBody());
    }


    @Test
    void shouldNotGetActor() {
        //given
        BigDecimal id = mock(BigDecimal.class);
        when(imdbService.findActor(id)).thenReturn(Optional.empty());

        //when
        ResponseEntity<Actor> result = sut.actorsIdGet(id);

        //then
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertNull(result.getBody());
    }
}