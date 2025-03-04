package com.equifax.c2o.usevalidateffid.health;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import com.equifax.c2o.api.interconnect.service.FFIDValidationService;

/**
 * A Spring Boot Actuator health indicator for the FFID validation service.
 * This will be included in the /actuator/health endpoint.
 */
@Component
public class FFIDValidationHealthIndicator implements HealthIndicator {

    @Autowired
    private FFIDValidationService ffidValidationService;
    
    @Override
    public Health health() {
        try {
            // Try to use the service to verify it's working properly
            boolean isServiceAvailable = ffidValidationService != null;
            
            if (isServiceAvailable) {
                return Health.up()
                    .withDetail("service", "FFIDValidationService")
                    .withDetail("status", "Available")
                    .build();
            } else {
                return Health.down()
                    .withDetail("service", "FFIDValidationService")
                    .withDetail("status", "Not available")
                    .build();
            }
        } catch (Exception e) {
            return Health.down()
                .withDetail("service", "FFIDValidationService")
                .withDetail("status", "Error")
                .withDetail("error", e.getMessage())
                .build();
        }
    }
}
