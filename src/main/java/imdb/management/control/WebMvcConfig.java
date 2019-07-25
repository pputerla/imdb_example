package imdb.management.control;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static imdb.management.control.SwaggerConfig.API_ANT_PATTERN;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private UserHitInterceptor userHitInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry
                .addInterceptor(userHitInterceptor)
                .addPathPatterns(API_ANT_PATTERN);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry
                .addResourceHandler("/webjars/**")
                .addResourceLocations("/webjars/")
                .resourceChain(false);
    }
}