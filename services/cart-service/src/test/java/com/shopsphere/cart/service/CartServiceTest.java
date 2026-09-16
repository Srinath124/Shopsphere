package com.shopsphere.cart.service;

import com.shopsphere.cart.dto.AddItemRequest;
import com.shopsphere.cart.dto.CartResponse;
import com.shopsphere.cart.dto.UpdateItemRequest;
import com.shopsphere.cart.model.Cart;
import com.shopsphere.cart.model.CartItem;
import com.shopsphere.cart.repository.CartRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CartService Unit Tests")
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @InjectMocks
    private CartService cartService;

    private static final String USER_ID = "user-123";
    private static final String PRODUCT_ID = "PROD-001";

    private Cart emptyCart;

    @BeforeEach
    void setUp() {
        emptyCart = Cart.builder().userId(USER_ID).build();
    }

    @Test
    @DisplayName("getCart - returns empty cart when user has no cart")
    void getCart_noExistingCart_returnsEmpty() {
        given(cartRepository.findByUserId(USER_ID)).willReturn(Optional.empty());

        CartResponse result = cartService.getCart(USER_ID);

        assertThat(result.getUserId()).isEqualTo(USER_ID);
        assertThat(result.getItems()).isEmpty();
        assertThat(result.getTotalItems()).isZero();
        assertThat(result.getTotalAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("getCart - returns existing cart with items")
    void getCart_existingCart_returnsItems() {
        CartItem item = CartItem.builder()
                .productId(PRODUCT_ID)
                .productName("Test Product")
                .unitPrice(new BigDecimal("29.99"))
                .quantity(2)
                .build();
        emptyCart.addItem(PRODUCT_ID, item);

        given(cartRepository.findByUserId(USER_ID)).willReturn(Optional.of(emptyCart));

        CartResponse result = cartService.getCart(USER_ID);

        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getTotalItems()).isEqualTo(2);
        assertThat(result.getTotalAmount()).isEqualByComparingTo(new BigDecimal("59.98"));
    }

    @Test
    @DisplayName("addItem - creates new cart and adds item when cart doesn't exist")
    void addItem_noExistingCart_createsAndAddsItem() {
        AddItemRequest request = new AddItemRequest();
        request.setProductId(PRODUCT_ID);
        request.setProductName("New Product");
        request.setUnitPrice(new BigDecimal("9.99"));
        request.setQuantity(3);
        request.setImageUrl("https://example.com/img.jpg");

        given(cartRepository.findByUserId(USER_ID)).willReturn(Optional.empty());
        given(cartRepository.save(any(Cart.class))).willAnswer(inv -> inv.getArgument(0));

        CartResponse result = cartService.addItem(USER_ID, request);

        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getTotalItems()).isEqualTo(3);
        then(cartRepository).should().save(any(Cart.class));
    }

    @Test
    @DisplayName("addItem - adds to existing product quantity when product already in cart")
    void addItem_productAlreadyInCart_incrementsQuantity() {
        CartItem existing = CartItem.builder()
                .productId(PRODUCT_ID)
                .productName("Test Product")
                .unitPrice(new BigDecimal("10.00"))
                .quantity(1)
                .build();
        emptyCart.addItem(PRODUCT_ID, existing);

        AddItemRequest request = new AddItemRequest();
        request.setProductId(PRODUCT_ID);
        request.setProductName("Test Product");
        request.setUnitPrice(new BigDecimal("10.00"));
        request.setQuantity(2);

        given(cartRepository.findByUserId(USER_ID)).willReturn(Optional.of(emptyCart));
        given(cartRepository.save(any(Cart.class))).willAnswer(inv -> inv.getArgument(0));

        CartResponse result = cartService.addItem(USER_ID, request);

        assertThat(result.getTotalItems()).isEqualTo(3);  // 1 + 2
    }

    @Test
    @DisplayName("updateItem - throws IllegalStateException when cart not found")
    void updateItem_cartNotFound_throwsException() {
        given(cartRepository.findByUserId(USER_ID)).willReturn(Optional.empty());
        UpdateItemRequest request = new UpdateItemRequest();
        request.setQuantity(5);

        assertThatThrownBy(() -> cartService.updateItem(USER_ID, PRODUCT_ID, request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(USER_ID);

        then(cartRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("clearCart - delegates to repository deleteByUserId")
    void clearCart_callsRepository() {
        willDoNothing().given(cartRepository).deleteByUserId(USER_ID);

        cartService.clearCart(USER_ID);

        then(cartRepository).should().deleteByUserId(USER_ID);
    }
}
