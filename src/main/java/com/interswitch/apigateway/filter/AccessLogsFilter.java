package com.interswitch.apigateway.filter;

import com.interswitch.apigateway.model.AccessLogs;
import com.interswitch.apigateway.model.AccessLogs.*;
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
            if (!isRouteBasedEndpoint && isAuditMethod(exchange.getRequest().getMethodValue())) {
                AccessLogs accessLogs = new AccessLogs();
                JWT token = filterUtil.decodeBearerToken(exchange.getRequest().getHeaders());
                String username = (token != null) ? filterUtil.getUsernameFromBearerToken(token) : "";
                String client = (token != null) ? filterUtil.getClientIdFromBearerToken(token) : "";
                String path = exchange.getRequest().getPath().toString();
                String method = exchange.getRequest().getMethodValue();
                LocalDateTime timestamp = LocalDateTime.now();
                Entity[] entities = Entity.values();
                Action[] actions = Action.values();
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

                if(accessLogs.getEntity().equals(Entity.SYSTEM)){
                    for(var actuatorEndpoint : ActuatorEndpoint.values()){
                        if(path.contains(actuatorEndpoint.getValue())){
                            if(actuatorEndpoint.equals(ActuatorEndpoint.ROUTE_REFRESH) || actuatorEndpoint.equals(ActuatorEndpoint.BUS_REFRESH)){
                                accessLogs.setEntityId("");
                                accessLogs.setAction(Action.REFRESH);
                            }
                            break;
                        }
                    }
                }
                accessLogs.setUsername(username);
                accessLogs.setClient(client);

                accessLogs.setApi(path);
                accessLogs.setTimestamp(timestamp);

                return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                    HttpStatus status = exchange.getResponse().getStatusCode() != null ? exchange.getResponse().getStatusCode() : HttpStatus.OK;
                    if(status.isError())
                        accessLogs.setStatus(AccessLogs.Status.FAILED);
                    else
                        accessLogs.setStatus(AccessLogs.Status.SUCCESSFUL);
                    LOG.info("Audit Log Event: timestamp: {}, username: {}, client: {}, api: {}, entity: {}, entityID: {}, action: {}, status: {}",
                            accessLogs.getTimestamp(),
                            accessLogs.getUsername(),
                            accessLogs.getClient(),
                            accessLogs.getApi(),
                            accessLogs.getEntity(),
                            accessLogs.getEntityId(),
                            accessLogs.getAction(),
                            accessLogs.getStatus());
                            mongoAccessLogsRepository.save(accessLogs).subscribe();
                }));
            }
        return chain.filter(exchange);
        });
    }

    private boolean isAuditMethod(String requestMethod){
        boolean isAuditMethod = false;
        Action[] methods = Action.values();
        for (var method : methods) {
            if(method.getValue().equals(requestMethod))
                isAuditMethod = true;
        }
        return isAuditMethod;
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
