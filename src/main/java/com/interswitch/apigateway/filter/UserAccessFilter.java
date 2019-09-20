package com.interswitch.apigateway.filter;

import com.interswitch.apigateway.model.User;
import com.interswitch.apigateway.repository.MongoUserRepository;
import com.interswitch.apigateway.util.RouteUtil;
import com.nimbusds.jwt.JWT;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

import static com.interswitch.apigateway.util.FilterUtil.decodeBearerToken;
import static com.interswitch.apigateway.util.FilterUtil.getClaimAsStringFromBearerToken;

public class UserAccessFilter implements WebFilter, Ordered {

    private static List<String> openSystemEndpoints = Arrays.asList("/actuator/health", "/actuator/prometheus");

    private static String openUserEndpoint = "/projects.*";

    private static List<String> adminEndpoints = Arrays.asList("/users.*", "/golive/approve.*", "/golive/decline.*");

    private MongoUserRepository mongoUserRepository;

    private RouteUtil routeUtil;

    public UserAccessFilter(MongoUserRepository mongoUserRepository, RouteUtil routeUtil) {
        this.mongoUserRepository = mongoUserRepository;
        this.routeUtil = routeUtil;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        HttpMethod method = exchange.getRequest().getMethod();
        String path = exchange.getRequest().getPath().toString();
        return routeUtil.isRouteBasedEndpoint(exchange).flatMap(isRouteBasedEndpoint -> {
            if (!isRouteBasedEndpoint && !openSystemEndpoints.contains(path) && !HttpMethod.OPTIONS.equals(method) && !path.matches(openUserEndpoint)) {
                JWT token = decodeBearerToken(exchange.getRequest().getHeaders());
                String username = getClaimAsStringFromBearerToken(token, "user_name");
                String email = getClaimAsStringFromBearerToken(token, "email");
                if (isInterswitchEmail(email)) {
                    for (var adminEndpoint : adminEndpoints) {
                        if (path.matches(adminEndpoint)) {
                            return mongoUserRepository.findByUsername(username)
                                    .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "You need administrative rights to access this resource")))
                                    .flatMap(user -> {
                                        if (user.getRole().equals(User.Role.ADMIN))
                                            return chain.filter(exchange);
                                        return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "You need administrative rights to access this resource"));
                                    });
                        }
                    }
                    return chain.filter(exchange);
                }
                return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "You need to exist as an Interswitch domain user before you can access this resource"));
            }
            return chain.filter(exchange);
        });
    }

    private boolean isInterswitchEmail(String email) {
        return email.endsWith("@interswitchgroup.com") ||
                email.endsWith("@interswitch.com") ||
                email.endsWith("@interswitchng.com");
    }

    @Override
    public int getOrder() {
        return -80;
    }
}