package com.shopsphere.cart.repository;

import com.shopsphere.cart.model.Cart;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CartRepository {

    private static final String CART_KEY_PREFIX = "cart:";
    private static final Duration CART_TTL = Duration.ofHours(24);

    private final RedisTemplate<String, Cart> redisTemplate;

    public Optional<Cart> findByUserId(String userId) {
        Cart cart = redisTemplate.opsForValue().get(cartKey(userId));
        return Optional.ofNullable(cart);
    }

    public Cart save(Cart cart) {
        redisTemplate.opsForValue().set(cartKey(cart.getUserId()), cart, CART_TTL);
        return cart;
    }

    public void deleteByUserId(String userId) {
        redisTemplate.delete(cartKey(userId));
    }

    public boolean existsByUserId(String userId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(cartKey(userId)));
    }

    private String cartKey(String userId) {
        return CART_KEY_PREFIX + userId;
    }
}
