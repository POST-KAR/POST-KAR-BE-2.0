package com.postkar.project3dmodel.config;

import com.postkar.project3dmodel.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledTasks {

    @Autowired
    private AuthService authService;

    @Scheduled(fixedRate = 3600000)
    public void cleanupExpiredRegistrations() {
        try {
            authService.cleanupExpiredRegistrations();
            System.out.println("Cleanup completed: Expired temp registrations removed");
        } catch (Exception e) {
            System.err.println("Error during cleanup: " + e.getMessage());
        }
    }
}
