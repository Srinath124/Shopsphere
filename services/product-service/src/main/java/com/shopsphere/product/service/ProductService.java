package com.shopsphere.product.service;

import com.shopsphere.product.domain.Category;
import com.shopsphere.product.domain.Product;
import com.shopsphere.product.dto.*;
import com.shopsphere.product.exception.DuplicateResourceException;
import com.shopsphere.product.exception.ResourceNotFoundException;
import com.shopsphere.product.repository.CategoryRepository;
import com.shopsphere.product.repository.ProductRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final Counter productCreatedCounter;
    private final Counter productUpdatedCounter;

    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository,
                          MeterRegistry meterRegistry) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productCreatedCounter = Counter.builder("shopsphere.products.created")
                .description("Number of products created")
                .register(meterRegistry);
        this.productUpdatedCounter = Counter.builder("shopsphere.products.updated")
                .description("Number of products updated")
                .register(meterRegistry);
    }

    public Page<ProductResponse> getProducts(String search, UUID categoryId, Pageable pageable) {
        Page<Product> products;
        if (StringUtils.hasText(search)) {
            products = productRepository.searchByNameOrDescription(search, pageable);
        } else if (categoryId != null) {
            products = productRepository.findByCategoryIdAndActiveTrue(categoryId, pageable);
        } else {
            products = productRepository.findByActiveTrue(pageable);
        }
        return products.map(this::mapToResponse);
    }

    public ProductResponse getProductById(UUID id) {
        Product product = productRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
        return mapToResponse(product);
    }

    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + request.getCategoryId()));

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .category(category)
                .imageUrl(request.getImageUrl())
                .active(request.isActive())
                .build();

        Product saved = productRepository.save(product);
        productCreatedCounter.increment();
        log.info("Created product: {} (id: {})", saved.getName(), saved.getId());
        return mapToResponse(saved);
    }

    @Transactional
    public ProductResponse updateProduct(UUID id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + request.getCategoryId()));

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setCategory(category);
        product.setImageUrl(request.getImageUrl());
        product.setActive(request.isActive());

        Product updated = productRepository.save(product);
        productUpdatedCounter.increment();
        log.info("Updated product: {}", updated.getId());
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteProduct(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
        product.setActive(false);
        productRepository.save(product);
        log.info("Soft-deleted product: {}", id);
    }

    public List<CategoryResponse> getCategories() {
        return categoryRepository.findAll().stream()
                .map(this::mapCategoryToResponse)
                .toList();
    }

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        if (categoryRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Category already exists: " + request.getName());
        }

        Category category = Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();

        Category saved = categoryRepository.save(category);
        log.info("Created category: {}", saved.getName());
        return mapCategoryToResponse(saved);
    }

    private ProductResponse mapToResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .categoryId(product.getCategory().getId())
                .categoryName(product.getCategory().getName())
                .imageUrl(product.getImageUrl())
                .active(product.isActive())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    private CategoryResponse mapCategoryToResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .createdAt(category.getCreatedAt())
                .build();
    }
}
