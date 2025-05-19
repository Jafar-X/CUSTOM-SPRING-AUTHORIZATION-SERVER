package com.example.spring.authorizationserver.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
//Why it required even though we already defined entity with prefix
@EnableJpaRepositories(
        basePackages = "com.example.spring.authorizationserver.client.repository",
        entityManagerFactoryRef = "clientEntityManagerFactory",
        transactionManagerRef = "clientTransactionManager"
)
public class ClientRepositoryConfig {
    // This class enables repositories in the client package to use the client database
}
