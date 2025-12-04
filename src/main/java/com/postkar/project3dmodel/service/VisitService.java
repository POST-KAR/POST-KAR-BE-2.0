package com.postkar.project3dmodel.service;

import com.postkar.project3dmodel.dto.RecordVisitRequest;
import com.postkar.project3dmodel.entity.VisitSession;
import com.postkar.project3dmodel.repository.VisitSessionRepository;
import com.postkar.project3dmodel.response.VisitStatsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class VisitService {

    private final VisitSessionRepository visitSessionRepository;
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    /**
     * Record a visit and return session ID
     * @param request - Visit request containing optional sessionId
     * @return Session ID
     */
    public String recordVisit(RecordVisitRequest request) {
        LocalDateTime now = LocalDateTime.now();
        
        // Check if session already exists
        if (request.getSessionId() != null && !request.getSessionId().isEmpty()) {
            Optional<VisitSession> existingSession = visitSessionRepository.findBySessionId(request.getSessionId());
            
            if (existingSession.isPresent()) {
                // Update existing session
                VisitSession session = existingSession.get();
                session.setLastActivity(now);
                session.setActive(true);
                visitSessionRepository.save(session);
                
                log.info("Updated existing visit session: {}", request.getSessionId());
                return session.getSessionId();
            }
        }
        
        // Create new session
        String sessionId = "session-" + UUID.randomUUID().toString();
        VisitSession newSession = new VisitSession();
        newSession.setSessionId(sessionId);
        newSession.setFirstVisit(now);
        newSession.setLastActivity(now);
        newSession.setActive(true);
        
        visitSessionRepository.save(newSession);
        log.info("Created new visit session: {}", sessionId);
        
        return sessionId;
    }

    /**
     * Get visit statistics
     * @return Visit statistics including total visits and active users
     */
    public VisitStatsResponse getVisitStats() {
        // Total visits = total number of sessions ever created
        long totalVisits = visitSessionRepository.count();
        
        // Active now = sessions active in last 5 minutes
        LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);
        long activeNow = visitSessionRepository.countByLastActivityAfter(fiveMinutesAgo);
        
        String lastUpdated = LocalDateTime.now().format(ISO_FORMATTER);
        
        log.info("Visit stats - Total: {}, Active: {}", totalVisits, activeNow);
        
        return new VisitStatsResponse(totalVisits, activeNow, lastUpdated);
    }
}
