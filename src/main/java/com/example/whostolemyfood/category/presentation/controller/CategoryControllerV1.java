package com.example.whostolemyfood.category.presentation.controller;

import com.example.whostolemyfood.category.application.service.CategoryServiceV1;
import com.example.whostolemyfood.category.presentation.dto.request.ReqCategoryDtoV1;
import com.example.whostolemyfood.category.presentation.dto.response.ResGetCategoryDtoV1;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/categories")
public class CategoryControllerV1 {
    private final CategoryServiceV1 categoryService;

    @GetMapping()
    public ResponseEntity<List<ResGetCategoryDtoV1>> getCategories() {
        return ResponseEntity.ok(categoryService.getCategories());
    }

    @GetMapping("/{category_id}")
    public ResponseEntity<ResGetCategoryDtoV1> getCategory(@PathVariable("category_id") UUID categoryId) {
        return ResponseEntity.ok(categoryService.getCategory(categoryId));
    }

    @PostMapping()
    public ResponseEntity<ResGetCategoryDtoV1> createCategory(@Valid @RequestBody ReqCategoryDtoV1 request) {
        ResGetCategoryDtoV1 response = categoryService.createCategory(request);
        return ResponseEntity.created(URI.create("/api/v1/categories/"+response.getCategoryId())).body(response);
    }

    @PutMapping("/{category_id}")
    public ResponseEntity<ResGetCategoryDtoV1> updateCategory(
            @PathVariable("category_id") UUID categoryId,
            @RequestBody @Valid ReqCategoryDtoV1 request
    ) {
        return ResponseEntity.ok(categoryService.updateCategory(categoryId, request));
    }

    @DeleteMapping("/{category_id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable("category_id") UUID categoryId) {
        categoryService.deleteCategory(categoryId);
        return ResponseEntity.noContent().build();
    }
}
