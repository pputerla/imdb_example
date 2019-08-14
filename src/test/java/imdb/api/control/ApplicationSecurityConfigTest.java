package imdb.api.control;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {ApplicationSecurityConfig.class})
@AutoConfigureMockMvc
class ApplicationSecurityConfigTest {

    private static final String ROLE_USER = "ROLE_USER";

    @Autowired
    private ApplicationContext ctx;

    @Autowired
    private AuthenticationManagerBuilder authenticationManagerBuilder;

    @Autowired
    private MockMvc mvc;


    @Test
    void shouldRequireAuth() throws Exception {
        mvc
                .perform(post("/api/someRequest"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectRequest() throws Exception {
        mvc
                .perform(post("/api/someRequest").header("Authorization", "Basic " + Base64.getEncoder().encodeToString("user1:pass1".getBytes())))
                .andExpect(status().isNotFound());
    }


    @Test
    void shouldNotRequireAuth() throws Exception {
        MvcResult res = mvc.perform(post("/someRequest")).andReturn();
        assertEquals(404, res.getResponse().getStatus());
    }

    @ParameterizedTest
    @CsvSource({"user1,pass1", "user2,pass2", "user3,pass3"})
    void shouldAuthenticateUsers(String user, String pass) {
        AuthenticationManager am = authenticationManagerBuilder.getObject();
        Authentication testingAuthenticationToken = new UsernamePasswordAuthenticationToken(user, pass);
        Authentication result = am.authenticate(testingAuthenticationToken);
        assertTrue(result.isAuthenticated());
        assertEquals(ROLE_USER, result.getAuthorities().iterator().next().getAuthority());
    }

    @ParameterizedTest
    @CsvSource({"user1,pass", "user2,pass", "user3,pass", "unknown_user,some_pass"})
    void shouldNotAuthenticateUsers(String user, String pass) {
        Assertions.assertThrows(BadCredentialsException.class, () -> {
            AuthenticationManager am = authenticationManagerBuilder.getObject();
            Authentication testingAuthenticationToken = new UsernamePasswordAuthenticationToken(user, pass);
            am.authenticate(testingAuthenticationToken);
        });
    }

    @Test
    void passwordEncoder() {
        PasswordEncoder passwordEncoder = ctx.getBean(PasswordEncoder.class);
        assertTrue(passwordEncoder instanceof BCryptPasswordEncoder, () -> "password encoder is " + passwordEncoder);
    }
}