package com.interswitch.apigateway;

import com.interswitch.apigateway.model.Client;
import com.interswitch.apigateway.repository.ClientMongoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
@WebFluxTest
@ActiveProfiles("dev")
@ContextConfiguration(classes = {ClientMongoRepository.class})

public class ApiGatewayApplicationTests {

	@MockBean
	ClientMongoRepository clientMongoRepository;

	@Test
	public void contextLoads() {
		assertThat("Alex").endsWith("x");
	}

}

