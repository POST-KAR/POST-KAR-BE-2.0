package com.postkar.project3dmodel.service;

import com.postkar.project3dmodel.entity.Model;
import com.postkar.project3dmodel.repository.ModelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ModelService {
    @Autowired
    private ModelRepository modelRepository;

    public List<Model> getModelsByCategory(String categoryId) {
        return modelRepository.findByCategoryId(categoryId);
    }

    public Optional<Model> getById(String id) {
        return modelRepository.findById(id);
    }

    public Model saveModel(Model model) {
        return modelRepository.save(model);
    }

    public List<Model> getAllModels(){
        return modelRepository.findAll();
    }
}

