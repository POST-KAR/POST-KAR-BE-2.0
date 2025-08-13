package com.postkar.project3dmodel.controller;

import com.postkar.project3dmodel.entity.Category;
import com.postkar.project3dmodel.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categories")
@Tag(name = "Categories", description = "APIs for managing categories of 3D models")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @Operation(summary = "Get All Categories", description = "Fetch all available categories " +
           "Example:- " + "{\n" +
            "  \"page\": 0,\n" +
            "  \"size\": 10,\n" +
            "  \"sort\": [\"name,ASC\", \"createdAt,DESC\"]\n" +
            "}\n" + "page: Which page to retrieve (0 means first page).\n" +
            "\n" +
            "size: 10 categories per page.\n" +
            "\n" +
            "sort: Sort first by name in ascending order. If names are the same, sort by createdAt in descending order." +
            "\n")
    @GetMapping
    public ResponseEntity<Page<Category>> getAll(Pageable pageable) {
        return ResponseEntity.ok(categoryService.getAllCategories(pageable));
    }
}