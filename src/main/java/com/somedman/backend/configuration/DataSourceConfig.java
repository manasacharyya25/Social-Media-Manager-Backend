package com.somedman.backend.configuration;

import com.somedman.backend.utills.ApplicationConstants;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {
    @Value("${db.poolSize}")
    private Integer maxDbPoolSize;

    @Value("${db.idleTimeout}")
    private Long idleTimeout;

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    @Bean
    public javax.sql.DataSource getDataSource() {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setMaximumPoolSize(maxDbPoolSize);
        hikariConfig.setIdleTimeout(idleTimeout);
        hikariConfig.setDriverClassName(ApplicationConstants.POSTGRES_DRIVER);
        hikariConfig.setJdbcUrl(dbUrl);
        hikariConfig.setUsername(dbUsername);
        hikariConfig.setPassword(dbPassword);
        return new HikariDataSource(hikariConfig);
    }


}
