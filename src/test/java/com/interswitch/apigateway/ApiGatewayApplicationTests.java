package com.interswitch.apigateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
@SpringBootTest
@ActiveProfiles("dev")
public class ApiGatewayApplicationTests {

	@Test
	public void contextLoads() {
		assertThat("Alex").endsWith("x");
	}

}

