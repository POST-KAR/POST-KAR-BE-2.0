package com.postkar.project3dmodel.controller;

import com.postkar.project3dmodel.entity.Model;
import com.postkar.project3dmodel.service.ModelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/models")
@Tag(name = "3D Models", description = "APIs for managing and retrieving 3D models")
public class ModelController {

    @Autowired
    private ModelService modelService;

    @GetMapping
    @Operation(summary = "Get Models by Category", description = "Retrieve all models under a specific category")
    public ResponseEntity<Page<Model>> getByCategory(@RequestParam String categoryId, Pageable pageable) {
        return ResponseEntity.ok(modelService.getModelsByCategory(categoryId, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Models by Model ID", description = "Retrieve a model associated with ID")
    public ResponseEntity<Model> getById(@PathVariable String id) {
        Optional<Model> optionalModel = modelService.getById(id);
        return optionalModel.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/all-models")
    @Operation(summary = "Get All Models", description = "Retrieve all 3D models")
    public ResponseEntity<Page<Model>> getAllModels(Pageable pageable) {
        return ResponseEntity.ok(modelService.getAllModels(pageable));
    }

    @PostMapping("/upload")
    @Operation(summary = "Upload a model/design", description = "Upload model/designs. Completely optional for now")
    public ResponseEntity<Model> upload(@RequestBody Model model) {
        return ResponseEntity.ok(modelService.saveModel(model));
    }
}