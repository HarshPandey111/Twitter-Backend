package com.microblog.twitterclone.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Configuration
public class DatabaseConfig {
    
    @Bean
    public DataSource dataSource() {
        String databaseUrl = System.getenv("DATABASE_URL");
        
        // Default to localhost for local development if DATABASE_URL not set
        if (databaseUrl == null || databaseUrl.isEmpty()) {
            databaseUrl = "jdbc:postgresql://localhost:5432/twitter";
        } else if (!databaseUrl.startsWith("jdbc:")) {
            // Railway provides DATABASE_URL as postgresql://..., convert to JDBC format
            databaseUrl = "jdbc:" + databaseUrl;
        }
        
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(databaseUrl);
        config.setMaximumPoolSize(5);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(20000);
        config.setIdleTimeout(300000);
        
        return new HikariDataSource(config);
    }
}
