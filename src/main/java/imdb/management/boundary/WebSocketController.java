package imdb.management.boundary;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class WebSocketController {

    private final UserStatisticsMBean userStatisticsMBean;


    @SubscribeMapping("/topic/hitCounts")
    public void initialReply() {
        userStatisticsMBean.hitCountHandler();
    }
}