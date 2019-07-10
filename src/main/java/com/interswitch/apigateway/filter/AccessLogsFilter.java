package com.interswitch.apigateway.filter;

import com.interswitch.apigateway.model.AccessLogs;
import com.interswitch.apigateway.repository.MongoAccessLogsRepository;
import com.interswitch.apigateway.util.FilterUtil;
import com.interswitch.apigateway.util.RouteUtil;
import com.nimbusds.jwt.JWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

public class AccessLogsFilter implements WebFilter, Ordered {

    private static final Logger LOG = LoggerFactory.getLogger(AccessLogsFilter.class);

    private MongoAccessLogsRepository mongoAccessLogsRepository;

    private FilterUtil filterUtil;

    private RouteUtil routeUtil;

    public AccessLogsFilter(MongoAccessLogsRepository mongoAccessLogsRepository, FilterUtil filterUtil, RouteUtil routeUtil) {
        this.mongoAccessLogsRepository = mongoAccessLogsRepository;
        this.filterUtil = filterUtil;
        this.routeUtil = routeUtil;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return routeUtil.isRouteBasedEndpoint(exchange).flatMap(isRouteBasedEndpoint -> {
            if (!isRouteBasedEndpoint) {
                AccessLogs accessLogs = new AccessLogs();
                JWT token = filterUtil.decodeBearerToken(exchange.getRequest().getHeaders());
                String username = (token != null) ? filterUtil.getUsernameFromBearerToken(token) : "";
                String path = exchange.getRequest().getPath().toString();
                String method = exchange.getRequest().getMethodValue();
                LocalDateTime timestamp = LocalDateTime.now();
                AccessLogs.Entity[] entities = AccessLogs.Entity.values();
                AccessLogs.Action[] actions = AccessLogs.Action.values();
                for (var entity : entities) {
                    if (path.contains(entity.getValue())) {
                        String id = getId(entity.getValue(), path);
                        accessLogs.setEntity(entity);
                        accessLogs.setEntityId(id);
                        break;
                    }
                }
                for (var action : actions) {
                    if (method.equals(action.getValue()))
                        accessLogs.setAction(action);

                }
                accessLogs.setUsername(username);
                accessLogs.setApi(path);
                accessLogs.setTimestamp(timestamp);

                return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                    HttpStatus status = exchange.getResponse().getStatusCode();
                    accessLogs.setResponseCode(status);
                    LOG.info("Audit Log Event: timestamp: {}, username: {}, api: {}, entity: {}, action: {}, entityID: {}, responseCode: {}",
                            accessLogs.getTimestamp(),
                            accessLogs.getUsername(),
                            accessLogs.getApi(),
                            accessLogs.getEntity(),
                            accessLogs.getAction(),
                            accessLogs.getEntityId(),
                            accessLogs.getResponseCode());
                    mongoAccessLogsRepository.save(accessLogs).subscribe();
                }));
            }
        return chain.filter(exchange);
        });
    }


    private String getId(String subPath, String fullPath){
        int endOfSubPath = fullPath.indexOf(subPath) + subPath.length();
        if(endOfSubPath == fullPath.length())
            return "";
        int indexOfId = endOfSubPath + 1;
        int nextSlashAfterSubPath = fullPath.indexOf('/', indexOfId);
        if(nextSlashAfterSubPath < 0)
            nextSlashAfterSubPath = fullPath.length();
        return fullPath.substring(indexOfId, nextSlashAfterSubPath);
    }
    @Override
    public int getOrder() {
        return -1000;
    }
}
