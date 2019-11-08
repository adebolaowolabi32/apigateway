package com.interswitch.apigateway.filter;

import com.interswitch.apigateway.model.AccessLogs;
import com.interswitch.apigateway.model.AccessLogs.Entity;
import com.interswitch.apigateway.model.AccessLogs.GoliveActions;
import com.interswitch.apigateway.model.AccessLogs.MethodActions;
import com.interswitch.apigateway.repository.MongoAccessLogsRepository;
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

import static com.interswitch.apigateway.util.FilterUtil.decodeBearerToken;
import static com.interswitch.apigateway.util.FilterUtil.getClaimAsStringFromBearerToken;

public class AccessLogsFilter implements WebFilter, Ordered {

    private static final Logger LOG = LoggerFactory.getLogger(AccessLogsFilter.class);

    private MongoAccessLogsRepository mongoAccessLogsRepository;

    private RouteUtil routeUtil;

    public AccessLogsFilter(MongoAccessLogsRepository mongoAccessLogsRepository, RouteUtil routeUtil) {
        this.mongoAccessLogsRepository = mongoAccessLogsRepository;
        this.routeUtil = routeUtil;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return routeUtil.isRouteBasedEndpoint(exchange).flatMap(isRouteBasedEndpoint -> {
            if (!isRouteBasedEndpoint && isAuditMethod(exchange.getRequest().getMethodValue())) {
                AccessLogs accessLogs = new AccessLogs();
                JWT token = decodeBearerToken(exchange.getRequest().getHeaders());
                String email = getClaimAsStringFromBearerToken(token, "email");
                String client = getClaimAsStringFromBearerToken(token, "client_id");
                String path = exchange.getRequest().getPath().toString();
                String method = exchange.getRequest().getMethodValue();
                LocalDateTime timestamp = LocalDateTime.now();
                for (var entity : Entity.values()) {
                    if (path.contains(entity.getValue())) {
                        accessLogs.setEntity(entity);
                        accessLogs.setEntityId(getId(entity.getValue(), path));
                        break;
                    }
                }
                if (accessLogs.getEntity() != null) {
                    if (accessLogs.getEntity().equals(Entity.GOLIVE))
                        for (var action : GoliveActions.values()) {
                            if (path.contains(action.getValue()))
                                accessLogs.setAction(action);
                        }
                    else if (accessLogs.getEntity().equals(Entity.REFRESH))
                        accessLogs.setAction(MethodActions.REFRESH);
                    else
                        for (var action : MethodActions.values()) {
                            if (method.equals(action.getValue()))
                                accessLogs.setAction(action);
                        }
                }

                accessLogs.setUsername(email);
                accessLogs.setClient(client);

                accessLogs.setApi(path);
                accessLogs.setTimestamp(timestamp);

                return chain.filter(exchange)
                        .doFinally((signalType) -> {
                            HttpStatus status = exchange.getResponse().getStatusCode();
                            if (status.isError())
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
                        });
            }
            return chain.filter(exchange);
        });
    }

    private boolean isAuditMethod(String requestMethod){
        boolean isAuditMethod = false;
        for (var method : MethodActions.values()) {
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
        return -50;
    }
}