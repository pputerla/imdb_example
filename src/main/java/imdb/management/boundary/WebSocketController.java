package imdb.management.boundary;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class WebSocketController {

    public static final String TOPIC_HIT_COUNTS = "/topic/hitCounts";
    private final UserStatisticsMBean userStatisticsMBean;


    @SubscribeMapping(TOPIC_HIT_COUNTS)
    public void initialReply() {
        userStatisticsMBean.hitCountHandler();
    }
}