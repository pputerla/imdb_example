package imdb.management.control;

import imdb.management.boundary.UserStatisticsMBean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UserHitInterceptor implements HandlerInterceptor {

    private final UserStatisticsMBean userStatisticsMBean;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        userStatisticsMBean.hit(request.getUserPrincipal());
        return true;
    }
}