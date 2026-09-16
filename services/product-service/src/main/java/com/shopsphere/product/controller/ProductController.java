package com.shopsphere.product.controller;

import com.shopsphere.product.dto.*;
import com.shopsphere.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Product catalog endpoints")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    @Operation(summary = "List products with optional search and filtering")
    public ResponseEntity<Page<ProductResponse>> getProducts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), sort);

        return ResponseEntity.ok(productService.getProducts(search, categoryId, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable UUID id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new product", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.createProduct(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a product", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable UUID id,
            @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Soft-delete a product", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> deleteProduct(@PathVariable UUID id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
