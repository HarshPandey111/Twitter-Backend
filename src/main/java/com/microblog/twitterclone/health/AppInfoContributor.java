package com.microblog.twitterclone.health;

import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
public class AppInfoContributor implements InfoContributor {

    @Override
    public void contribute(Info.Builder builder) {
        Map<String, Object> details = new HashMap<>();
        details.put("startup-time", LocalDateTime.now().toString());
        details.put("features", new String[]{
                "User Authentication",
                "Tweet Management",
                "Follow System",
                "Timeline/Feed",
                "Redis Caching",
                "Kafka Events",
                "WebSocket Real-time",
                "Docker Deployment",
                "CI/CD Pipeline"
        });
        details.put("tech-stack", new String[]{
                "Spring Boot 3.2",
                "PostgreSQL",
                "Redis",
                "Apache Kafka",
                "WebSocket",
                "Docker"
        });

        builder.withDetail("application", details);
    }
}
