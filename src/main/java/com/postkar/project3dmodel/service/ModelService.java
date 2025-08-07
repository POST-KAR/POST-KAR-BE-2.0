package com.postkar.project3dmodel.service;

import com.postkar.project3dmodel.entity.Model;
import com.postkar.project3dmodel.repository.ModelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ModelService {
    @Autowired
    private ModelRepository modelRepository;

    public Page<Model> getModelsByCategory(String categoryId, Pageable pageable) {
        return modelRepository.findByCategoryId(categoryId, pageable);
    }

    public Optional<Model> getById(String id) {
        return modelRepository.findById(id);
    }

    public Model saveModel(Model model) {
        return modelRepository.save(model);
    }

    public Page<Model> getAllModels(Pageable pageable) {
        return modelRepository.findAll(pageable);
    }
}