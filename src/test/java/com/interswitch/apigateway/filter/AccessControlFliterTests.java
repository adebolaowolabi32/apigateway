package com.interswitch.apigateway.filter;

import com.interswitch.apigateway.config.CacheConfig;
import com.interswitch.apigateway.controller.ClientResourcesController;
import com.interswitch.apigateway.model.ClientResources;
import com.interswitch.apigateway.repository.ClientResourcesRepository;
import com.interswitch.apigateway.repository.MongoClientResourcesRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@WebFluxTest
@ActiveProfiles("dev")
@ContextConfiguration(classes = {MongoClientResourcesRepository.class,ClientResourcesRepository.class, AccessControlFilter.class})
@Import(CacheConfig.class)
public class AccessControlFliterTests {

    @Autowired
    private ClientResourcesRepository repository;
    @MockBean
    private MongoClientResourcesRepository mongo;

    private AccessControlFilter filter;
    private GatewayFilterChain filterChain;
    private ArgumentCaptor<ServerWebExchange> captor;
    private List testresourceIds = new ArrayList();
    private ClientResources resource;
    private String  clientId = "testclientid";

    @BeforeEach
    public void setup() {
        filter = new AccessControlFilter(repository);
        filterChain = mock(GatewayFilterChain.class);
        captor = ArgumentCaptor.forClass(ServerWebExchange.class);
        testresourceIds.add("passport/oauth/token");
        testresourceIds.add("passport/oauth/authorize");
        resource = new ClientResources("id",clientId,testresourceIds);
    }

    @Test
    public void testAccessControl (){
        when(repository.findByClientId(resource.getClientId())).thenReturn(Mono.empty());
        MockServerHttpRequest request = MockServerHttpRequest
                .options("http://localhost")
                .header("Authorization","Bearer eyJhbGciOiJSUzI1NiJ9.eyJsYXN0TmFtZSI6IlRlc3RlciIsIm1lcmNoYW50X2NvZGUiOiJRVEVMTCIsInByb2R1Y3Rpb25fcGF5bWVudF9jb2RlIjoiMDUxNDYzNTEyNTY3MCIsImZpcnN0TG9naW4iOmZhbHNlLCJ1c2VyX25hbWUiOiJpc3d0ZXN0ZXIyQHlhaG9vLmNvbSIsInJlcXVlc3Rvcl9pZCI6IjAwMTYxMDkwOTA0IiwibW9iaWxlTm8iOiIyMzQ4MDU2NzMxNTc2IiwicGF5YWJsZV9pZCI6IjExNCIsImNsaWVudF9pZCI6IklLSUEzNDRCODkwMDk3MDAxNjQ3RUVEQjYwMjI2QTU4NTBBRTc1QzdDRDE5IiwiZmlyc3ROYW1lIjoiVGVzdGVycyIsImVtYWlsVmVyaWZpZWQiOnRydWUsImF1ZCI6WyJjYWVzYXIiLCJmaW5nZXJwcmludC1hcGkiLCJpbmNvZ25pdG8iLCJpc3ctY29sbGVjdGlvbnMiLCJpc3ctY29yZSIsImlzdy1pbnN0aXR1dGlvbiIsImlzdy1wYXltZW50Z2F0ZXdheSIsInBhc3Nwb3J0IiwicHJvamVjdC14LW1lcmNoYW50IiwidmF1bHQiXSwic2NvcGUiOlsicHJvZmlsZSJdLCJleHAiOjE1NTIwMDQ1MTEsIm1vYmlsZU5vVmVyaWZpZWQiOnRydWUsImp0aSI6ImU1MWYzZTE4LTFmZDMtNGYxZS1iOTg1LTFjZDI4MDMzYWExNiIsImVtYWlsIjoiaXN3dGVzdGVyMkB5YWhvby5jb20iLCJwYXNzcG9ydElkIjoiRTI1MUYwRTktN0JDRi00Q0FFLThFOEItNTZERjI1RUQ4NUQwIiwicGF5bWVudF9jb2RlIjoiMDUxNDYzNTEyNTY3MCJ9.OIcZkMfYoR1eq23nbMrgKHilCuSO3TyyGuUazcyIt7iJXpfLPjU3Y9byHcmz3nMmR7t3x3crztWDBKJUIuua-hyT36MBCzbpeXQmOswl1INWPGoEat_f5IMMXCGdU6W50XhFy4n4s4IAlrAQqGQrHiW7fHrFFrJeu3AVHl8utr-g560W2PlDyDGUss3aOt-plduXY6IFb6gTfoLsm2ZCGIR8s2lBmeSooSNDrw7ak368F2scbUhaFaHA4pDEas05AXO6dWh_qI9G_kJ40Loj_jCnacEdenVpiWJLyO2wBmQqPaMKW5MW_NOLgGEcXR-eRlN4n4NCtb2onE1y-DyQMQ")
                .build();
        assertAuthorizationHeader(request);
    }


    @Test
    public ServerWebExchange assertAuthorizationHeader(MockServerHttpRequest request) {
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        when(filterChain.filter(captor.capture())).thenReturn(Mono.empty());
        filter.filter(exchange, filterChain).block();
        ServerWebExchange webExchange = captor.getValue();

        assertThat(webExchange.getResponse().getHeaders().containsKey("Authorization"));
        return webExchange;
    }
}
