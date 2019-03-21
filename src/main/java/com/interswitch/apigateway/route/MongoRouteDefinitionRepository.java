package com.interswitch.apigateway.route;

import com.interswitch.apigateway.repository.ReactiveMongoRouteDefinitionRepository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.filter.factory.GatewayFilterFactory;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.handler.predicate.RoutePredicateFactory;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MongoRouteDefinitionRepository implements RouteDefinitionRepository {

    private ReactiveMongoRouteDefinitionRepository mongo;
    private final Map<String, GatewayFilterFactory> gatewayFilterFactories = new HashMap<>();
    private final Map<String, RoutePredicateFactory> predicates = new LinkedHashMap<>();
    protected final Log logger = LogFactory.getLog(getClass());

    public MongoRouteDefinitionRepository(ReactiveMongoRouteDefinitionRepository mongo,
                                          List<GatewayFilterFactory> gatewayFilterFactories,
                                          List<RoutePredicateFactory> predicates) {
        gatewayFilterFactories.forEach(
                factory -> this.gatewayFilterFactories.put(factory.name(), factory));
        this.mongo = mongo;
        initFactories(predicates);
    }

    @Override
    public Flux<RouteDefinition> getRouteDefinitions() {
        return mongo.findAll();
    }

    @Override
    public Mono<Void> save(@Validated Mono<RouteDefinition> route) {
        return route.flatMap(rou -> {
            List<PredicateDefinition> predicates = rou.getPredicates();
            List<FilterDefinition> filters = rou.getFilters();
            if (checkGatewayFilters(filters)==true && checkGatewayPredicates(predicates)==true|| filters.size()==0 ){
                    return mongo.save(rou).then();
            }
            if(checkGatewayPredicates(predicates)==false){
                return  Mono.error(
                        new ResponseStatusException(HttpStatus.BAD_REQUEST, "Gateway Predicate(s) Does Not Exist"))  ;
            }
            else{
                return  Mono.error(
                        new ResponseStatusException(HttpStatus.BAD_REQUEST, "Gateway Filter(s) Does Not Exist"))  ;
        }
        });
    }

    @Override
    public Mono<Void> delete(Mono<String> routeId) {
        return mongo.deleteById(routeId);
    }


    //methods to check the body of the request before saving to database.
    private Boolean checkGatewayFilters(List<FilterDefinition> filterDefinitions) {
        return filterDefinitions.stream().allMatch(filterDefinition -> {
            GatewayFilterFactory factory = this.gatewayFilterFactories.get(filterDefinition.getName());
            if(factory == null){
                return false;
            }
            else{return true;}
        })    ;

    }

    private Boolean checkGatewayPredicates (List<PredicateDefinition> predicateDefinitions){
        return predicateDefinitions.stream().allMatch(predicateDefinition -> {
            RoutePredicateFactory<Object> factory = this.predicates.get(predicateDefinition.getName());
            if (factory == null) {
                return false;
            }
            else{
                return true;
            }
        });
    }

    //method to initialize predicate factory to a form that can be matched to the request.
    private void initFactories(List<RoutePredicateFactory> predicates) {
        predicates.forEach(factory -> {
            String key = factory.name();
            if (this.predicates.containsKey(key)) {
                this.logger.warn("A RoutePredicateFactory named " + key
                        + " already exists, class: " + this.predicates.get(key)
                        + ". It will be overwritten.");
            }
            this.predicates.put(key, factory);
            if (logger.isInfoEnabled()) {
                logger.info("Loaded RoutePredicateFactory [" + key + "]");
            }
        });
    }

}
