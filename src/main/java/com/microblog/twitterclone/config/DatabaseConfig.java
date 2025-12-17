package com.microblog.twitterclone.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class DatabaseConfig {
    
    @Bean
    public DataSource dataSource() {
        String databaseUrl = System.getenv("DATABASE_URL");
        
        String jdbcUrl = "jdbc:postgresql://localhost:5432/twitter"; // default fallback
        String username = "postgres";
        String password = "password123";
        
        if (databaseUrl != null && !databaseUrl.isEmpty()) {
            try {
                // Railway provides: postgresql://user:password@host:port/database
                if (databaseUrl.startsWith("postgresql://")) {
                    // Extract components using string manipulation
                    String afterProtocol = databaseUrl.substring("postgresql://".length());
                    
                    // Split by @ to separate credentials from host
                    String[] credentialsAndHost = afterProtocol.split("@", 2);
                    
                    if (credentialsAndHost.length == 2) {
                        // Extract username and password
                        String[] credentials = credentialsAndHost[0].split(":", 2);
                        username = credentials[0];
                        if (credentials.length > 1) {
                            password = credentials[1];
                        }
                        
                        // Extract host:port/database
                        String hostPortDb = credentialsAndHost[1];
                        String[] hostAndDb = hostPortDb.split("/", 2);
                        String hostPort = hostAndDb[0];
                        String dbName = hostAndDb.length > 1 ? hostAndDb[1] : "twitter";
                        
                        String[] hostParts = hostPort.split(":");
                        String host = hostParts[0];
                        int port = hostParts.length > 1 ? Integer.parseInt(hostParts[1]) : 5432;
                        
                        jdbcUrl = String.format("jdbc:postgresql://%s:%d/%s", host, port, dbName);
                    }
                } else if (databaseUrl.startsWith("jdbc:")) {
                    // Already in JDBC format
                    jdbcUrl = databaseUrl;
                    // Try to extract username/password if available, otherwise use defaults
                } else {
                    // Unknown format, try prefixing with jdbc:
                    jdbcUrl = "jdbc:" + databaseUrl;
                }
            } catch (Exception e) {
                System.err.println("Error parsing DATABASE_URL: " + e.getMessage());
                // Keep default fallback
            }
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
