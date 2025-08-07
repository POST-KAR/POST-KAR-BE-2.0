package com.postkar.project3dmodel.controller;

import com.postkar.project3dmodel.entity.Category;
import com.postkar.project3dmodel.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public ResponseEntity<Page<Category>> getAll(Pageable pageable) {
        return ResponseEntity.ok(categoryService.getAllCategories(pageable));
    }
}