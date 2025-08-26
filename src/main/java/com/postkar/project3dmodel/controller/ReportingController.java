package com.postkar.project3dmodel.controller;

import com.postkar.project3dmodel.dto.DetectionReportRequest;
import com.postkar.project3dmodel.service.ReportingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/reports")
@Tag(name = "Reporting", description = "APIs for detection and usage reporting")
public class ReportingController {

    @Autowired
    private ReportingService reportingService;

    @Operation(
        summary = "Report Marker Detection",
        description = "Report when a marker is detected and video playback occurs.\n\n" +
                     "Apps should call this endpoint to provide analytics data:\n" +
                     "- When marker is successfully detected\n" +
                     "- Video playback success/failure\n" +
                     "- Detection performance metrics\n\n" +
                     "This data helps optimize marker design and track usage."
    )
    @PostMapping("/detection")
    public ResponseEntity<String> reportDetection(@Valid @RequestBody DetectionReportRequest request) {
        reportingService.recordDetection(request);
        return ResponseEntity.ok("Detection reported successfully");
    }
}
