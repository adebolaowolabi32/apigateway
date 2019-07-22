package com.interswitch.apigateway.repository;

import com.interswitch.apigateway.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("dev")
@DataMongoTest
public class MongoUserRepositoryTests extends AbstractMongoRepositoryTests {
    @Autowired
    MongoUserRepository mongoUserRepository;

    @Test
    public void testFindAll(){
        User u1 = new User();
        u1.setId("userId1");
        u1.setUsername("testusernameone");
        User u2 = new User();
        u2.setId("userId2");
        u2.setUsername("testusernametwo");
        mongoUserRepository.save(u1).block();
        mongoUserRepository.save(u2).block();
        StepVerifier.create(mongoUserRepository.findAll()).expectNextCount(2);
    }

    @Test
    public void testFindByUsername(){
        User user = new User();
        user.setId("userId");
        user.setUsername("testusername");
        User savedUser = mongoUserRepository.save(user).block();
        StepVerifier.create(mongoUserRepository.findByUsername(user.getUsername())).assertNext(u -> {
            assertThat(u.getId()).isEqualTo(user.getId()).isEqualTo(savedUser.getId());
            assertThat(u.getRole()).isEqualTo(user.getRole()).isEqualTo(savedUser.getRole());
        }).expectComplete().verify();
    }

    @Test
    public void testUpdate(){
        User user = new User();
        user.setId("userId");
        user.setUsername("testusername");
        User savedUser = mongoUserRepository.save(user).block();
        savedUser.setUsername("usernametwo");
        savedUser.setRole(User.Role.ADMIN);
        mongoUserRepository.save(user).block();
        StepVerifier.create(mongoUserRepository.findById(user.getId())).assertNext(u -> {
            assertThat(u.getUsername()).isEqualTo(savedUser.getUsername());
            assertThat(u.getRole()).isEqualTo(savedUser.getRole());
        }).expectComplete().verify();
    }

    @Test
    public void testDelete(){
        User user = new User();
        user.setId("userId");
        user.setUsername("testusername");
        User savedUser = mongoUserRepository.save(user).block();
        mongoUserRepository.deleteById(savedUser.getId()).block();
        StepVerifier.create(mongoUserRepository.findById(savedUser.getId())).expectComplete().verify();
    }
}
