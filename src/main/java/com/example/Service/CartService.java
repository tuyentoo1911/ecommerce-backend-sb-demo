package com.example.Service;

import org.springframework.stereotype.Service;
import com.example.javaspringboot.repository.*;
import com.example.javaspringboot.entity.*;
import com.example.dto.request.CartItemRequest;
import com.example.dto.response.CartItemResponse;
import com.example.dto.response.ProductResponse;
import com.example.mapper.ProductMapper;
import com.example.exception.AppException;
import com.example.exception.Errorcode;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.AccessLevel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CartService {
    CartItemRepository cartItemRepository;
    ProductRepository productRepository;
    UserRepository userRepository;
    ProductMapper productMapper;

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(Errorcode.USER_NOT_FOUND));
    }

    public List<CartItemResponse> getCart() {
        User user = getCurrentUser();
        return cartItemRepository.findByUser(user)
                .stream()
                .map(this::toCartItemResponse)
                .toList();
    }

    @Transactional
    public CartItemResponse addToCart(CartItemRequest request) {
        User user = getCurrentUser();
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new AppException(Errorcode.PRODUCT_NOT_FOUND));

        if (product.getStock() < request.getQuantity()) {
            throw new AppException(Errorcode.INSUFFICIENT_STOCK);
        }

        CartItem cartItem = cartItemRepository.findByUserAndProductId(user, product.getId())
                .map(existing -> {
                    existing.setQuantity(existing.getQuantity() + request.getQuantity());
                    return existing;
                })
                .orElseGet(() -> CartItem.builder()
                        .id(UUID.randomUUID().toString())
                        .user(user)
                        .product(product)
                        .quantity(request.getQuantity())
                        .build());

        return toCartItemResponse(cartItemRepository.save(cartItem));
    }

    @Transactional
    public CartItemResponse updateCartItem(String id, CartItemRequest request) {
        User user = getCurrentUser();
        CartItem cartItem = cartItemRepository.findById(id)
                .orElseThrow(() -> new AppException(Errorcode.CART_ITEM_NOT_FOUND));

        if (!cartItem.getUser().getId().equals(user.getId())) {
            throw new AppException(Errorcode.ACCESS_DENIED);
        }

        if (cartItem.getProduct().getStock() < request.getQuantity()) {
            throw new AppException(Errorcode.INSUFFICIENT_STOCK);
        }

        cartItem.setQuantity(request.getQuantity());
        return toCartItemResponse(cartItemRepository.save(cartItem));
    }

    @Transactional
    public void removeCartItem(String id) {
        User user = getCurrentUser();
        CartItem cartItem = cartItemRepository.findById(id)
                .orElseThrow(() -> new AppException(Errorcode.CART_ITEM_NOT_FOUND));

        if (!cartItem.getUser().getId().equals(user.getId())) {
            throw new AppException(Errorcode.ACCESS_DENIED);
        }

        cartItemRepository.deleteById(id);
    }

    private CartItemResponse toCartItemResponse(CartItem cartItem) {
        ProductResponse productResponse = productMapper.toResponse(cartItem.getProduct());
        BigDecimal subtotal = cartItem.getProduct().getPrice()
                .multiply(BigDecimal.valueOf(cartItem.getQuantity()));

        return CartItemResponse.builder()
                .id(cartItem.getId())
                .product(productResponse)
                .quantity(cartItem.getQuantity())
                .subtotal(subtotal)
                .build();
    }
}
