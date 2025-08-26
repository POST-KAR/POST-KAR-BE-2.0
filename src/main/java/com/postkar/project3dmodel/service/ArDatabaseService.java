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

    public Optional<MarkerVersion> getCurrentVersion() {
        return markerVersionRepository.findByIsActiveTrue();
    }

    public Optional<ArDatabase> getCurrentDatabase() {
        return arDatabaseRepository.findByIsActiveTrue();
    }

    public void triggerDatabaseRebuild() {
        if (!buildEnabled) {
            return;
        }

        ArDatabase newDb = new ArDatabase();
        newDb.setVersion(generateNewVersion());
        newDb.setBuildStatus("building");
        newDb.setCreatedAt(LocalDateTime.now());
        newDb.setActive(false);

        ArDatabase savedDb = arDatabaseRepository.save(newDb);

        databaseBuildService.buildDatabaseAsync(savedDb.getId());
    }

    public void publishDatabase(String databaseId) {
        Optional<ArDatabase> dbOpt = arDatabaseRepository.findById(databaseId);

        if (dbOpt.isPresent() && "ready".equals(dbOpt.get().getBuildStatus())) {
            ArDatabase newDb = dbOpt.get();

            arDatabaseRepository.findByIsActiveTrue()
                    .ifPresent(currentDb -> {
                        currentDb.setActive(false);
                        arDatabaseRepository.save(currentDb);
                    });

            newDb.setActive(true);
            newDb.setPublishedAt(LocalDateTime.now());
            arDatabaseRepository.save(newDb);

            updateVersionInfo(newDb);
        }
    }

    private void updateVersionInfo(ArDatabase database) {
        markerVersionRepository.findByIsActiveTrue()
                .ifPresent(currentVersion -> {
                    currentVersion.setActive(false);
                    markerVersionRepository.save(currentVersion);
                });

        MarkerVersion newVersion = new MarkerVersion();
        newVersion.setVersion(database.getVersion());
        newVersion.setArDatabaseId(database.getId());
        newVersion.setLastUpdated(LocalDateTime.now());
        newVersion.setActive(true);
        newVersion.setTotalMarkers(markerRepository.findByIsActiveTrue().size());

        markerVersionRepository.save(newVersion);
    }

    private String generateNewVersion() {
        Optional<MarkerVersion> latestVersion = markerVersionRepository.findTopByOrderByLastUpdatedDesc();

        if (latestVersion.isPresent()) {
            String currentVersion = latestVersion.get().getVersion();
            int versionNumber = Integer.parseInt(currentVersion.substring(1)) + 1;
            return "v" + versionNumber;
        }

        return "v1";
    }
}