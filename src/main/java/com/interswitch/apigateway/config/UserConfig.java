package com.interswitch.apigateway.config;

import com.interswitch.apigateway.model.User;
import com.interswitch.apigateway.repository.MongoUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserConfig {
    @Bean
    public CommandLineRunner commandLineRunr(MongoUserRepository mongoUserRepository){
        return commandLineRunr -> {
           User user = new User();
           user.setId("systemAdmin");
           user.setUsername("systemAdministrator");
           user.setRole(User.Role.ADMIN);
           mongoUserRepository.save(user).subscribe();
        };
    }
}
