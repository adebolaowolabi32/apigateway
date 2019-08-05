package com.interswitch.apigateway.route;

import com.interswitch.apigateway.repository.ReactiveMongoRouteDefinitionRepository;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.filter.factory.GatewayFilterFactory;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.handler.predicate.RoutePredicateFactory;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionRepository;
import org.springframework.cloud.gateway.support.ConfigurationUtils;
import org.springframework.core.convert.ConversionService;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.http.HttpStatus;
import org.springframework.validation.Validator;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

public class MongoRouteDefinitionRepository implements RouteDefinitionRepository {

    private ReactiveMongoRouteDefinitionRepository mongo;
    private List<GatewayFilterFactory> gatewayFilterFactories;
    private List<RoutePredicateFactory> routePredicateFactories;
    private final SpelExpressionParser parser = new SpelExpressionParser();
    private BeanFactory beanFactory;
    private ConversionService conversionService;
    private Validator validator;

    public MongoRouteDefinitionRepository(ReactiveMongoRouteDefinitionRepository mongo,
                                          List<GatewayFilterFactory> gatewayFilterFactories,
                                          List<RoutePredicateFactory> routePredicateFactories,
                                          Validator validator,
                                          @Qualifier("webFluxConversionService") ConversionService conversionService,
                                          BeanFactory beanFactory) {
        this.mongo = mongo;
        this.gatewayFilterFactories=gatewayFilterFactories;
        this.routePredicateFactories=routePredicateFactories;
        this.validator=validator;
        this.conversionService=conversionService;
        this.beanFactory=beanFactory;
    }

    @Override
    public Flux<RouteDefinition> getRouteDefinitions() {
        return mongo.findAll();
    }

//    public Mono<RouteDefinition> getRouteDefinition(String id){
//        return mongo.findById(id);
//    }


    @Override
    public Mono<Void> save(@Validated Mono<RouteDefinition> route) {
        return route.flatMap(r -> {
            List<PredicateDefinition> predicates = r.getPredicates();
            List<FilterDefinition> filters = r.getFilters();
            checkGatewayPredicatesExist(predicates);
            checkGatewayFiltersExists(filters);
            return mongo.save(r).then();
        });
    }

    @Override
    public Mono<Void> delete(Mono<String> routeId) {
        return mongo.deleteById(routeId).then();
    }

    private void checkGatewayFiltersExists(List<FilterDefinition> filterDefinitions) {
        filterDefinitions.forEach(filterDefinition -> {
            this.gatewayFilterFactories.stream()
                    .filter(filterFactory -> filterFactory.name().equals(filterDefinition.getName())).findFirst()
                    .ifPresentOrElse(filterFactory -> {
                        Map<String, String> args = filterDefinition.getArgs();
                        Map<String, Object> properties = filterFactory.shortcutType().normalize(args,
                                filterFactory, this.parser, this.beanFactory);
                        Object configuration = filterFactory.newConfig();
                        try{ConfigurationUtils.bind(configuration, properties,
                                filterFactory.shortcutFieldPrefix(), filterDefinition.getName(), validator,
                                conversionService);}
                                catch (Exception e){
                            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to bind "+filterFactory.name()+" filter arguments");
                                }
                    }, () ->{throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Gateway Filter "+filterDefinition.getName()+" does not Exist");});
        });
    }

    private void checkGatewayPredicatesExist(List<PredicateDefinition> predicateDefinitions) {
        predicateDefinitions.forEach(predicateDefinition -> {
            this.routePredicateFactories.stream()
                    .filter(predicateFactory -> predicateFactory.name().equals(predicateDefinition.getName())).findFirst()
                    .ifPresentOrElse(predicateFactory -> {
                Map<String, String> args = predicateDefinition.getArgs();
                Map<String, Object> properties = predicateFactory.shortcutType().normalize(args,
                        predicateFactory, this.parser, this.beanFactory);
                Object configuration = predicateFactory.newConfig();
                    try{ConfigurationUtils.bind(configuration, properties,
                            predicateFactory.shortcutFieldPrefix(), predicateDefinition.getName(), validator,
                            conversionService);}
                            catch (Exception e){
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to bind "+predicateFactory.name()+" predicate arguments");
                            }
            }, () ->{throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Gateway Predicate "+predicateDefinition.getName()+" does not Exist");});
        });
    }
}
