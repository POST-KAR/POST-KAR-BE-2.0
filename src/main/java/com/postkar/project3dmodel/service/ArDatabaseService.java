package com.postkar.project3dmodel.service;

import com.postkar.project3dmodel.entity.ArDatabase;
import com.postkar.project3dmodel.entity.MarkerVersion;
import com.postkar.project3dmodel.repository.ArDatabaseRepository;
import com.postkar.project3dmodel.repository.MarkerVersionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ArDatabaseService {

    @Autowired
    private ArDatabaseRepository arDatabaseRepository;

    @Autowired
    private MarkerVersionRepository markerVersionRepository;

    public Optional<MarkerVersion> getCurrentVersion() {
        return markerVersionRepository.findByIsActiveTrue();
    }

    public Optional<ArDatabase> getCurrentDatabase() {
        return arDatabaseRepository.findByIsActiveTrue();
    }
}