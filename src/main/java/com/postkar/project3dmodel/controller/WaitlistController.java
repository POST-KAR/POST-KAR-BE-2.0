package com.postkar.project3dmodel.controller;

import com.postkar.project3dmodel.dto.WaitlistRequestDto;
import com.postkar.project3dmodel.dto.WaitlistResponseDto;
import com.postkar.project3dmodel.service.WaitlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/waitlist")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Waitlist", description = "Waitlist management APIs")
@CrossOrigin(origins = "*")
public class WaitlistController {

    private final WaitlistService waitlistService;

    @PostMapping("/join")
    @Operation(summary = "Join waitlist", description = "Add a new user to the waitlist")
    public ResponseEntity<Map<String, Object>> joinWaitlist(@Valid @RequestBody WaitlistRequestDto requestDto) {
        log.info("Received waitlist join request for email: {}", requestDto.getEmail());
        
        try {
            WaitlistResponseDto response = waitlistService.addToWaitlist(requestDto);
            
            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("success", true);
            responseMap.put("message", "Successfully joined the waitlist!");
            responseMap.put("data", response);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(responseMap);
        } catch (IllegalArgumentException e) {
            log.error("Error adding to waitlist: {}", e.getMessage());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
        } catch (Exception e) {
            log.error("Unexpected error adding to waitlist", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "An error occurred while processing your request");
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/all")
    @Operation(summary = "Get all waitlist entries", description = "Retrieve all waitlist entries (Admin only)")
    public ResponseEntity<Map<String, Object>> getAllWaitlistEntries() {
        log.info("Fetching all waitlist entries");
        
        List<WaitlistResponseDto> entries = waitlistService.getAllWaitlistEntries();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("count", entries.size());
        response.put("data", entries);
        
        return ResponseEntity.ok(response);
    }

//    @GetMapping("/count")
//    @Operation(summary = "Get waitlist count", description = "Get the total number of waitlist entries")
//    public ResponseEntity<Map<String, Object>> getWaitlistCount() {
//        log.info("Fetching waitlist count");
//
//        long count = waitlistService.getWaitlistCount();
//
//        Map<String, Object> response = new HashMap<>();
//        response.put("success", true);
//        response.put("count", count);
//
//        return ResponseEntity.ok(response);
//    }
}
