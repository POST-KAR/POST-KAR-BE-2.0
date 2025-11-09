package com.postkar.project3dmodel.controller;

import com.postkar.project3dmodel.dto.RecordVisitRequest;
import com.postkar.project3dmodel.response.VisitStatsResponse;
import com.postkar.project3dmodel.service.VisitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/visits")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Visit Counter", description = "Visit tracking and statistics APIs")
@CrossOrigin(origins = "*")
public class VisitController {

    private final VisitService visitService;

    @GetMapping("/stats")
    @Operation(summary = "Get visit statistics", description = "Retrieve current visit statistics including total visits and active users")
    public ResponseEntity<Map<String, Object>> getVisitStats() {
        log.info("Fetching visit statistics");
        
        try {
            VisitStatsResponse stats = visitService.getVisitStats();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", stats);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching visit stats", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to fetch visit statistics");
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/record")
    @Operation(summary = "Record a visit", description = "Record a new visit or update an existing session")
    public ResponseEntity<Map<String, Object>> recordVisit(@RequestBody RecordVisitRequest request) {
        log.info("Recording visit for session: {}", request.getSessionId());
        
        try {
            String sessionId = visitService.recordVisit(request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Visit recorded successfully");
            response.put("sessionId", sessionId);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Error recording visit", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to record visit");
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
