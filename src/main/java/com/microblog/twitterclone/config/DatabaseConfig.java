package com.microblog.twitterclone.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URISyntaxException;

@Configuration
public class DatabaseConfig {
    
    @Bean
    public DataSource dataSource() throws URISyntaxException {
        String databaseUrl = System.getenv("DATABASE_URL");
        
        String jdbcUrl;
        String username = "postgres";
        String password = "password123";
        
        if (databaseUrl != null && !databaseUrl.isEmpty()) {
            // Railway provides DATABASE_URL in format: postgresql://user:password@host:port/database
            // We need to convert it to JDBC format: jdbc:postgresql://host:port/database
            
            if (databaseUrl.startsWith("jdbc:")) {
                // Already in JDBC format
                jdbcUrl = databaseUrl;
            } else if (databaseUrl.startsWith("postgresql://")) {
                // Parse Railway's postgresql URL
                URI uri = new URI("postgresql" + databaseUrl.substring(10));
                username = uri.getUserInfo().split(":")[0];
                password = uri.getUserInfo().split(":")[1];
                
                String host = uri.getHost();
                int port = uri.getPort() != -1 ? uri.getPort() : 5432;
                String database = uri.getPath().substring(1); // Remove leading slash
                
                jdbcUrl = String.format("jdbc:postgresql://%s:%d/%s", host, port, database);
            } else {
                // Fallback: assume it's already a valid URL, just add jdbc: prefix
                jdbcUrl = "jdbc:" + databaseUrl;
            }
        } else {
            // Local development fallback
            jdbcUrl = "jdbc:postgresql://localhost:5432/twitter";
        }
        
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(5);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(20000);
        config.setIdleTimeout(300000);
        config.setMaxLifetime(1200000);
        
        return new HikariDataSource(config);
    }
}
