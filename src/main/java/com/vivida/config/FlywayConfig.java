package com.vivida.config;

import org.flywaydb.core.Flyway;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.core.env.Environment;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class FlywayConfig {

    @Bean
    public static BeanFactoryPostProcessor flywayMigrationBootstrap(Environment environment) {
        return beanFactory -> migrateDatabase(environment);
    }

    private static void migrateDatabase(Environment environment) throws BeansException {
        boolean flywayEnabled = environment.getProperty("spring.flyway.enabled", Boolean.class, true);
        if (!flywayEnabled) {
            return;
        }

        String url = environment.getProperty("spring.datasource.url");
        String username = environment.getProperty("spring.datasource.username");
        String password = environment.getProperty("spring.datasource.password");

        if (url == null || username == null || password == null) {
            throw new IllegalStateException("Spring datasource properties are required for Flyway bootstrap");
        }

        Flyway.configure()
                .dataSource(url, username, password)
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .baselineVersion("1")
                .load()
                .migrate();
    }
}