package com.postkar.project3dmodel.controller;

import com.postkar.project3dmodel.entity.Model;
import com.postkar.project3dmodel.service.ModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/models")
public class ModelController {

    @Autowired
    private ModelService modelService;

    @GetMapping
    public ResponseEntity<Page<Model>> getByCategory(@RequestParam String categoryId, Pageable pageable) {
        return ResponseEntity.ok(modelService.getModelsByCategory(categoryId, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Model> getById(@PathVariable String id) {
        Optional<Model> optionalModel = modelService.getById(id);
        return optionalModel.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/all-models")
    public ResponseEntity<Page<Model>> getAllModels(Pageable pageable) {
        return ResponseEntity.ok(modelService.getAllModels(pageable));
    }

    @PostMapping("/upload")
    public ResponseEntity<Model> upload(@RequestBody Model model) {
        return ResponseEntity.ok(modelService.saveModel(model));
    }
}