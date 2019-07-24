package imdb.api.boundary;

import imdb.api.control.ImdbService;
import io.swagger.api.ActorsApi;
import io.swagger.model.Actor;
import io.swagger.model.Appearance;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api")
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ActorController implements ActorsApi {

    private final ImdbService imdbService;

    @Override
    public ResponseEntity<List<Actor>> actorsGet(BigDecimal page, BigDecimal pageSize, String name) {
        return ResponseEntity.ok(imdbService.findActors(page, pageSize, name));

    }

    @Override
    public ResponseEntity<List<Appearance>> actorsIdAppearancesGet(BigDecimal actorId, BigDecimal page, BigDecimal pageSize) {
        return ResponseEntity.ok(imdbService.findAppearance(actorId, page, pageSize));
    }

    @Override
    public ResponseEntity<Actor> actorsIdGet(BigDecimal id) {
        return imdbService
                .findActor(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

}
