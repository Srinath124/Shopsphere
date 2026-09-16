package com.shopsphere.order.repository;

import com.shopsphere.order.domain.Order;
import com.shopsphere.order.domain.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    List<Order> findByUserIdOrderByCreatedAtDesc(String userId);

    Optional<Order> findByIdAndUserId(UUID id, String userId);

    Optional<Order> findByIdempotencyKey(String idempotencyKey);

    List<Order> findByStatus(OrderStatus status);
}
