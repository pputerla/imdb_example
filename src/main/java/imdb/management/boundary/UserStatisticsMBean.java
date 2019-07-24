package imdb.management.boundary;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;

import java.security.Principal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ManagedResource(
        objectName = "imdb:category=Statistics,name=userStatistics",
        description = "User Hit Count")
@Service
public class UserStatisticsMBean {


    @Autowired
    private SimpMessagingTemplate webSocket;

    private Map<String, Long> userHitCount = new ConcurrentHashMap<>();

    @ManagedAttribute
    public Map<String, Long> getUserHitCount() {
        return userHitCount;
    }

    public void hit(Principal userPrincipal) {
        userHitCount.merge(userPrincipal.getName(), 1L, (o, n) -> o + 1);
        hitCountHandler();
    }

    public void hitCountHandler() {
        webSocket.convertAndSend("/topic/hitCounts", userStatsMessage());
    }

    private WebSocketMessage userStatsMessage() {
        return new TextMessage(userHitCount.toString());
    }




}