package com.example.spring.authorizationserver.config;

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import java.util.HashMap;
import java.util.Map;

/**
 * Base configuration for both client and user datasources
 */
@Configuration
public class DataSourceConfig {
    // Client database configuration (sso)
    @Primary
    @Bean(name = "clientDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.client")
    public DataSource clientDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Primary
    @Bean(name = "clientJpaProperties")
    @ConfigurationProperties(prefix = "spring.jpa.client")
    public JpaProperties clientJpaProperties() {
        return new JpaProperties();
    }

    //to see if the property is automatically populated based on application properties using prefix
    //To check if Entity manager works
    //Scanning to the parent
    //to check if different database from different vendor work. if gets picked by dialect
    //If we use @Transaction in service layer which uses two database, what will happen if one database fails
    //to check if works with different JpaProvider without touching the code
    @Primary
    @Bean(name = "clientEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean clientEntityManagerFactory(
            @Qualifier("clientDataSource") DataSource dataSource,
            @Qualifier("clientJpaProperties") JpaProperties jpaProperties) {

        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("com.example.spring.authorizationserver.client.model");
        //to check if works with different JpaProvider without touching the code
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        em.setJpaPropertyMap(jpaProperties.getProperties());

        return em;
    }

    @Primary
    @Bean(name = "clientTransactionManager")
    public PlatformTransactionManager clientTransactionManager(
            @Qualifier("clientEntityManagerFactory") LocalContainerEntityManagerFactoryBean clientEntityManagerFactory) {
        return new JpaTransactionManager(clientEntityManagerFactory.getObject());
    }

    // User database configuration (app)
    @Bean(name = "userDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.user")
    public DataSource userDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "userJpaProperties")
    @ConfigurationProperties(prefix = "spring.jpa.user")
    public JpaProperties userJpaProperties() {
        return new JpaProperties();
    }

    @Bean(name = "userEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean userEntityManagerFactory(
            @Qualifier("userDataSource") DataSource dataSource,
            @Qualifier("userJpaProperties") JpaProperties jpaProperties) {

        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("com.example.spring.authorizationserver.user.model");

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);

        em.setJpaPropertyMap(jpaProperties.getProperties());

        return em;
    }

    @Bean(name = "userTransactionManager")
    public PlatformTransactionManager userTransactionManager(
            @Qualifier("userEntityManagerFactory") LocalContainerEntityManagerFactoryBean userEntityManagerFactory) {
        return new JpaTransactionManager(userEntityManagerFactory.getObject());
    }
}