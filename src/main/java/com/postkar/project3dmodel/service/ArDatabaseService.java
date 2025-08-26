package com.postkar.project3dmodel.service;

import com.postkar.project3dmodel.entity.ArDatabase;
import com.postkar.project3dmodel.entity.MarkerVersion;
import com.postkar.project3dmodel.repository.ArDatabaseRepository;
import com.postkar.project3dmodel.repository.MarkerRepository;
import com.postkar.project3dmodel.repository.MarkerVersionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ArDatabaseService {

    @Autowired
    private ArDatabaseRepository arDatabaseRepository;

    @Autowired
    private MarkerVersionRepository markerVersionRepository;

    @Autowired
    private MarkerRepository markerRepository;

    @Autowired
    private DatabaseBuildService databaseBuildService;

    @Value("${ar.database.build.enabled:true}")
    private boolean buildEnabled;

    // Get current active version info
    public Optional<MarkerVersion> getCurrentVersion() {
        return markerVersionRepository.findByIsActiveTrue();
    }

    // Get current AR database
    public Optional<ArDatabase> getCurrentDatabase() {
        return arDatabaseRepository.findByIsActiveTrue();
    }

    // Trigger database rebuild (async)
    public void triggerDatabaseRebuild() {
        if (!buildEnabled) {
            return;
        }

        // Create new database entry
        ArDatabase newDb = new ArDatabase();
        newDb.setVersion(generateNewVersion());
        newDb.setBuildStatus("building");
        newDb.setCreatedAt(LocalDateTime.now());
        newDb.setActive(false);

        ArDatabase savedDb = arDatabaseRepository.save(newDb);

        // Start async build process
        databaseBuildService.buildDatabaseAsync(savedDb.getId());
    }

    // Publish a built database (make it active)
    public void publishDatabase(String databaseId) {
        Optional<ArDatabase> dbOpt = arDatabaseRepository.findById(databaseId);

        if (dbOpt.isPresent() && "ready".equals(dbOpt.get().getBuildStatus())) {
            ArDatabase newDb = dbOpt.get();

            // Deactivate current database
            arDatabaseRepository.findByIsActiveTrue()
                    .ifPresent(currentDb -> {
                        currentDb.setActive(false);
                        arDatabaseRepository.save(currentDb);
                    });

            // Activate new database
            newDb.setActive(true);
            newDb.setPublishedAt(LocalDateTime.now());
            arDatabaseRepository.save(newDb);

            // Update version info
            updateVersionInfo(newDb);
        }
    }

    private void updateVersionInfo(ArDatabase database) {
        // Deactivate current version
        markerVersionRepository.findByIsActiveTrue()
                .ifPresent(currentVersion -> {
                    currentVersion.setActive(false);
                    markerVersionRepository.save(currentVersion);
                });

        // Create new version
        MarkerVersion newVersion = new MarkerVersion();
        newVersion.setVersion(database.getVersion());
        newVersion.setArDatabaseId(database.getId());
        newVersion.setLastUpdated(LocalDateTime.now());
        newVersion.setActive(true);
        newVersion.setTotalMarkers(markerRepository.findByIsActiveTrue().size());

        markerVersionRepository.save(newVersion);
    }

    private String generateNewVersion() {
        // Simple versioning: v1, v2, v3...
        Optional<MarkerVersion> latestVersion = markerVersionRepository.findTopByOrderByLastUpdatedDesc();

        if (latestVersion.isPresent()) {
            String currentVersion = latestVersion.get().getVersion();
            int versionNumber = Integer.parseInt(currentVersion.substring(1)) + 1;
            return "v" + versionNumber;
        }

        return "v1";
    }
}