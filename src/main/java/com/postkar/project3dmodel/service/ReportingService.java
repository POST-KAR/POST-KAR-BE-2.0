package com.postkar.project3dmodel.service;

import com.postkar.project3dmodel.dto.DetectionReportRequest;
import com.postkar.project3dmodel.entity.DetectionReport;
import com.postkar.project3dmodel.repository.DetectionReportRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ReportingService {
    
    private static final Logger logger = LoggerFactory.getLogger(ReportingService.class);
    
    @Autowired
    private DetectionReportRepository detectionReportRepository;

    @Async
    public void recordDetection(DetectionReportRequest request) {
        try {
            DetectionReport report = new DetectionReport();
            report.setMarkerId(request.getMarkerId());
            report.setDeviceId(request.getDeviceId());
            report.setDetectionTime(request.getDetectionTime());
            report.setAppVersion(request.getAppVersion());
            report.setPlatform(request.getPlatform());
            report.setDetectionDistance(request.getDetectionDistance());
            report.setDetectionDurationMs(request.getDetectionDurationMs());
            report.setVideoId(request.getVideoId());
            report.setPlaybackSuccess(request.getPlaybackSuccess());
            report.setCreatedAt(LocalDateTime.now());
            
            detectionReportRepository.save(report);
            
            logger.info("Detection recorded: marker={}, device={}, platform={}", 
                request.getMarkerId(), request.getDeviceId(), request.getPlatform());
            
        } catch (Exception e) {
            logger.error("Failed to record detection", e);
        }
    }
}
