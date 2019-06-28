package com.interswitch.apigateway;

import com.interswitch.apigateway.model.User;
import com.interswitch.apigateway.repository.MongoUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("dev")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DefaultSystemUserTests {
    @Autowired
    private MongoUserRepository mongoUserRepository;

    @Test
    public void testForDefaultSystemUser(){
        StepVerifier.create(mongoUserRepository.findByUsername("systemAdministrator")).assertNext(u -> {
            assertThat(u.getId()).isEqualTo("systemAdmin");
            assertThat(u.getRole()).isEqualTo(User.Role.ADMIN);
        }).expectComplete().verify();
    }
}