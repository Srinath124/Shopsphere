package com.shopsphere.product.service;

import com.shopsphere.product.domain.Category;
import com.shopsphere.product.domain.Product;
import com.shopsphere.product.dto.ProductRequest;
import com.shopsphere.product.dto.ProductResponse;
import com.shopsphere.product.exception.ResourceNotFoundException;
import com.shopsphere.product.repository.CategoryRepository;
import com.shopsphere.product.repository.ProductRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService Unit Tests")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    private ProductService productService;

    private Category testCategory;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        MeterRegistry meterRegistry = new SimpleMeterRegistry();
        productService = new ProductService(productRepository, categoryRepository, meterRegistry);

        testCategory = new Category();
        testCategory.setId(UUID.randomUUID());
        testCategory.setName("Electronics");

        testProduct = Product.builder()
                .id(UUID.randomUUID())
                .name("Test Laptop")
                .description("A test laptop")
                .price(new BigDecimal("999.99"))
                .category(testCategory)
                .imageUrl("https://example.com/laptop.jpg")
                .active(true)
                .build();
    }

    @Test
    @DisplayName("getProducts - returns paged results when no filters applied")
    void getProducts_noFilters_returnsPaged() {
        PageRequest pageable = PageRequest.of(0, 10);
        Page<Product> page = new PageImpl<>(List.of(testProduct), pageable, 1);
        given(productRepository.findByActiveTrue(pageable)).willReturn(page);

        Page<ProductResponse> result = productService.getProducts(null, null, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Test Laptop");
        then(productRepository).should().findByActiveTrue(pageable);
    }

    @Test
    @DisplayName("getProducts - delegates to search when search term provided")
    void getProducts_withSearchTerm_delegatesToSearch() {
        PageRequest pageable = PageRequest.of(0, 10);
        Page<Product> page = new PageImpl<>(List.of(testProduct), pageable, 1);
        given(productRepository.searchByNameOrDescription(eq("Laptop"), eq(pageable))).willReturn(page);

        Page<ProductResponse> result = productService.getProducts("Laptop", null, pageable);

        assertThat(result.getContent()).hasSize(1);
        then(productRepository).should().searchByNameOrDescription("Laptop", pageable);
        then(productRepository).should(never()).findByActiveTrue(any());
    }

    @Test
    @DisplayName("getProductById - returns product when found")
    void getProductById_found_returnsResponse() {
        UUID id = testProduct.getId();
        given(productRepository.findByIdAndActiveTrue(id)).willReturn(Optional.of(testProduct));

        ProductResponse result = productService.getProductById(id);

        assertThat(result.getName()).isEqualTo("Test Laptop");
        assertThat(result.getPrice()).isEqualByComparingTo(new BigDecimal("999.99"));
    }

    @Test
    @DisplayName("getProductById - throws ResourceNotFoundException when not found")
    void getProductById_notFound_throwsException() {
        UUID id = UUID.randomUUID();
        given(productRepository.findByIdAndActiveTrue(id)).willReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(id.toString());
    }

    @Test
    @DisplayName("createProduct - saves and returns product when category exists")
    void createProduct_validRequest_createsProduct() {
        ProductRequest request = new ProductRequest();
        request.setName("New Phone");
        request.setDescription("A new smartphone");
        request.setPrice(new BigDecimal("599.99"));
        request.setCategoryId(testCategory.getId());
        request.setImageUrl("https://example.com/phone.jpg");
        request.setActive(true);

        given(categoryRepository.findById(testCategory.getId())).willReturn(Optional.of(testCategory));
        given(productRepository.save(any(Product.class))).willAnswer(inv -> {
            Product p = inv.getArgument(0);
            p.setId(UUID.randomUUID());
            return p;
        });

        ProductResponse result = productService.createProduct(request);

        assertThat(result.getName()).isEqualTo("New Phone");
        assertThat(result.getPrice()).isEqualByComparingTo(new BigDecimal("599.99"));
        then(productRepository).should().save(any(Product.class));
    }

    @Test
    @DisplayName("createProduct - throws ResourceNotFoundException when category not found")
    void createProduct_categoryNotFound_throwsException() {
        ProductRequest request = new ProductRequest();
        request.setName("Widget");
        request.setPrice(BigDecimal.TEN);
        UUID badCategoryId = UUID.randomUUID();
        request.setCategoryId(badCategoryId);

        given(categoryRepository.findById(badCategoryId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> productService.createProduct(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(badCategoryId.toString());

        then(productRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("deleteProduct - soft deletes by setting active=false")
    void deleteProduct_existing_softDeletes() {
        UUID id = testProduct.getId();
        given(productRepository.findById(id)).willReturn(Optional.of(testProduct));
        given(productRepository.save(any(Product.class))).willReturn(testProduct);

        productService.deleteProduct(id);

        assertThat(testProduct.isActive()).isFalse();
        then(productRepository).should().save(testProduct);
    }

    @Test
    @DisplayName("deleteProduct - throws ResourceNotFoundException when product missing")
    void deleteProduct_notFound_throwsException() {
        UUID id = UUID.randomUUID();
        given(productRepository.findById(id)).willReturn(Optional.empty());

        assertThatThrownBy(() -> productService.deleteProduct(id))
                .isInstanceOf(ResourceNotFoundException.class);

        then(productRepository).should(never()).save(any());
    }
}
