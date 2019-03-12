package com.interswitch.apigateway.filter;

import com.interswitch.apigateway.config.CacheConfig;
import com.interswitch.apigateway.repository.ClientCacheRepository;
import com.interswitch.apigateway.repository.ClientMongoRepository;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.SocketUtils;

@RunWith(SpringRunner.class)
@ActiveProfiles("dev")
@WebFluxTest
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,properties = "management.server.port=${test.port}")
@DirtiesContext
@EnableAutoConfiguration
@ContextConfiguration(classes = {CacheConfig.class, ClientCacheRepository.class, ClientMongoRepository.class, AccessControlFilter.class})
public class AccessControlIntegrationTests  {
    @Autowired
    private WebTestClient testClient;

    private static int managementPort;
    @BeforeClass
    public static void beforeEach(){
        managementPort= SocketUtils.findAvailableTcpPort();
        System.setProperty("test.port",String.valueOf(managementPort));
    }
    @AfterClass
    public static void afterClass() {
        System.clearProperty("test.port");
    }

    @Test
    public void testAccessControl(){
        testClient.mutate().baseUrl("http://localhost:" +managementPort).build()
                .get().uri("/users/find/TestP.jaMonJan14").exchange();
    }

    @SpringBootConfiguration
    public static class TestConfig{

    }
}
