package com.postkar.project3dmodel.repository;

import com.postkar.project3dmodel.entity.DetectionReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DetectionReportRepository extends MongoRepository<DetectionReport, String> {
    
    Page<DetectionReport> findByMarkerIdOrderByDetectionTimeDesc(String markerId, Pageable pageable);
    
    Page<DetectionReport> findByDeviceIdOrderByDetectionTimeDesc(String deviceId, Pageable pageable);
    
    @Query("{ 'detectionTime': { $gte: ?0, $lte: ?1 } }")
    List<DetectionReport> findByDetectionTimeBetween(LocalDateTime start, LocalDateTime end);
    
    @Query("{ 'markerId': ?0, 'detectionTime': { $gte: ?1, $lte: ?2 } }")
    List<DetectionReport> findByMarkerIdAndDetectionTimeBetween(String markerId, LocalDateTime start, LocalDateTime end);
    
    long countByMarkerIdAndDetectionTimeBetween(String markerId, LocalDateTime start, LocalDateTime end);
}
