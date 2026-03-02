package com.example.Service;

import org.springframework.stereotype.Service;
import com.example.javaspringboot.repository.*;
import com.example.javaspringboot.entity.*;
import com.example.dto.request.CartItemRequest;
import com.example.dto.response.OrderResponse;
import com.example.dto.response.OrderItemResponse;
import com.example.enums.OrderStatus;
import com.example.exception.AppException;
import com.example.exception.Errorcode;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.AccessLevel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderService {
    OrderRepository orderRepository;
    CartItemRepository cartItemRepository;
    UserRepository userRepository;
    ProductRepository productRepository;

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(Errorcode.USER_NOT_FOUND));
    }

    /**
     * Order thẳng luôn: mua ngay 1 sản phẩm, không qua giỏ hàng.
     */
    @Transactional
    public OrderResponse buyNow(CartItemRequest request) {
        User user = getCurrentUser();
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new AppException(Errorcode.PRODUCT_NOT_FOUND));

        if (product.getStock() < request.getQuantity()) {
            throw new AppException(Errorcode.INSUFFICIENT_STOCK);
        }

        Order order = Order.builder()
                .id(UUID.randomUUID().toString())
                .user(user)
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        product.setStock(product.getStock() - request.getQuantity());
        productRepository.save(product);

        OrderItem orderItem = OrderItem.builder()
                .id(UUID.randomUUID().toString())
                .order(order)
                .product(product)
                .quantity(request.getQuantity())
                .priceAtPurchase(product.getPrice())
                .build();

        BigDecimal total = product.getPrice().multiply(BigDecimal.valueOf(request.getQuantity()));
        order.setTotalPrice(total);
        order.setItems(List.of(orderItem));

        return toOrderResponse(orderRepository.save(order));
    }

    @Transactional
    public OrderResponse checkout() {
        User user = getCurrentUser();
        List<CartItem> cartItems = cartItemRepository.findByUser(user);

        if (cartItems.isEmpty()) {
            throw new AppException(Errorcode.CART_EMPTY);
        }

        Order order = Order.builder()
                .id(UUID.randomUUID().toString())
                .user(user)
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        List<OrderItem> orderItems = cartItems.stream().map(cartItem -> {
            Product product = cartItem.getProduct();
            
            if (product.getStock() < cartItem.getQuantity()) {
                throw new AppException(Errorcode.INSUFFICIENT_STOCK);
            }

            product.setStock(product.getStock() - cartItem.getQuantity());
            productRepository.save(product);

            return OrderItem.builder()
                    .id(UUID.randomUUID().toString())
                    .order(order)
                    .product(product)
                    .quantity(cartItem.getQuantity())
                    .priceAtPurchase(product.getPrice())
                    .build();
        }).toList();

        BigDecimal total = orderItems.stream()
                .map(item -> item.getPriceAtPurchase().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setTotalPrice(total);
        order.setItems(orderItems);

        Order savedOrder = orderRepository.save(order);
        cartItemRepository.deleteByUser(user);

        return toOrderResponse(savedOrder);
    }

    public List<OrderResponse> getMyOrders() {
        User user = getCurrentUser();
        return orderRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(this::toOrderResponse)
                .toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll()
                .stream()
                .map(this::toOrderResponse)
                .toList();
    }

    @Transactional
    public OrderResponse cancelOrder(String orderId) {
        User user = getCurrentUser();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(Errorcode.ORDER_NOT_FOUND));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new AppException(Errorcode.ACCESS_DENIED);
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new AppException(Errorcode.ORDER_CANNOT_CANCEL);
        }

        order.getItems().forEach(item -> {
            Product product = item.getProduct();
            product.setStock(product.getStock() + item.getQuantity());
            productRepository.save(product);
        });

        order.setStatus(OrderStatus.CANCELLED);
        return toOrderResponse(orderRepository.save(order));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public OrderResponse updateOrderStatus(String orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(Errorcode.ORDER_NOT_FOUND));
        
        order.setStatus(status);
        return toOrderResponse(orderRepository.save(order));
    }

    private OrderResponse toOrderResponse(Order order) {
        List<OrderItemResponse> items = order.getItems().stream()
                .map(item -> OrderItemResponse.builder()
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .quantity(item.getQuantity())
                        .priceAtPurchase(item.getPriceAtPurchase())
                        .subtotal(item.getPriceAtPurchase().multiply(BigDecimal.valueOf(item.getQuantity())))
                        .build())
                .toList();

        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUser().getId())
                .username(order.getUser().getUsername())
                .totalPrice(order.getTotalPrice())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .items(items)
                .build();
    }
}
