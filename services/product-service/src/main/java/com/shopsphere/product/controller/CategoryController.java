package com.shopsphere.product.controller;

import com.shopsphere.product.dto.CategoryRequest;
import com.shopsphere.product.dto.CategoryResponse;
import com.shopsphere.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Product category endpoints")
public class CategoryController {

    private final ProductService productService;

    @GetMapping
    @Operation(summary = "List all categories")
    public ResponseEntity<List<CategoryResponse>> getCategories() {
        return ResponseEntity.ok(productService.getCategories());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new category")
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.createCategory(request));
    }
}
