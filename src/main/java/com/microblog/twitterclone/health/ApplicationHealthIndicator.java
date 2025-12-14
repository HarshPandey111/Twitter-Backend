package com.microblog.twitterclone.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;

@Component
public class ApplicationHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        try {
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            long usedMemory = memoryBean.getHeapMemoryUsage().getUsed();
            long maxMemory = memoryBean.getHeapMemoryUsage().getMax();

            double memoryUsagePercent = (double) usedMemory / maxMemory * 100;

            Health.Builder builder = memoryUsagePercent < 90 ? Health.up() : Health.down();

            return builder
                    .withDetail("app", "Twitter Clone API")
                    .withDetail("status", "Running")
                    .withDetail("memory_used_mb", usedMemory / (1024 * 1024))
                    .withDetail("memory_max_mb", maxMemory / (1024 * 1024))
                    .withDetail("memory_usage_percent", String.format("%.2f%%", memoryUsagePercent))
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
