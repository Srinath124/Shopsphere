package com.shopsphere.product.repository;

import com.shopsphere.product.domain.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    Page<Product> findByActiveTrue(Pageable pageable);

    Page<Product> findByCategoryIdAndActiveTrue(UUID categoryId, Pageable pageable);

    @Query("""
        SELECT p FROM Product p
        WHERE p.active = true
        AND (LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%'))
             OR LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%')))
        """)
    Page<Product> searchByNameOrDescription(@Param("search") String search, Pageable pageable);

    Optional<Product> findByIdAndActiveTrue(UUID id);
}
