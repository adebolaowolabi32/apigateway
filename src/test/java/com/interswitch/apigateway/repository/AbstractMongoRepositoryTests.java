package com.interswitch.apigateway.repository;

import com.interswitch.apigateway.config.TestMongoConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;

@Import(TestMongoConfig.class)
@DataMongoTest(excludeAutoConfiguration = EmbeddedMongoAutoConfiguration.class)
@Slf4j
public abstract class AbstractMongoRepositoryTests {
    @Autowired
    protected MongoTemplate template;

    @BeforeEach
    public void dbName() {
        log.info(template.getDb().getName());
    }
}
