package imdb.api.boundary;

import com.weddini.throttling.Throttling;
import com.weddini.throttling.ThrottlingType;
import imdb.api.control.ImdbService;
import io.swagger.api.MoviesApi;
import io.swagger.model.Movie;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController()
@RequestMapping("/api")
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MovieController implements MoviesApi {

    private final ImdbService imdbService;

    @Throttling(type = ThrottlingType.PrincipalName, limit = 5, timeUnit = TimeUnit.MINUTES)
    @Override
    public ResponseEntity<List<Movie>> moviesGet(BigDecimal page, BigDecimal pageSize, String name) {
        return ResponseEntity.ok(imdbService.findMovies(page, pageSize, name));
    }

    @Override
    public ResponseEntity<Movie> moviesIdGet(BigDecimal id) {
        return imdbService
                .findMovie(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
