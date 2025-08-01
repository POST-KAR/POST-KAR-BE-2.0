package com.postkar.project3dmodel.controller;

import com.postkar.project3dmodel.entity.Model;
import com.postkar.project3dmodel.service.ModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/models")
public class ModelController {

    @Autowired
    private ModelService modelService;

    @GetMapping
    public List<Model> getByCategory(@RequestParam String categoryId) {
        return modelService.getModelsByCategory(categoryId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Model> getById(@PathVariable String id) {
        Optional<Model> optionalModel = modelService.getById(id);

        if (optionalModel.isPresent()) {
            Model model = optionalModel.get();
            return ResponseEntity.ok(model);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/all-models")
    public List<Model> getAllModels(){
        return modelService.getAllModels();
    }

//    @PostMapping("/upload")
//    public ResponseEntity<Model> upload(@RequestBody Model model) {
//        return ResponseEntity.ok(modelService.saveModel(model));
//    }
}

