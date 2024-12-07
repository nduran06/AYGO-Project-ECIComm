package com.aygo.eciComm.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import com.aygo.eciComm.exception.OrderNotFoundException;
import com.aygo.eciComm.exception.OrderValidationException;
import com.aygo.eciComm.exception.ProductNotFoundException;
import com.aygo.eciComm.model.Order;
import com.aygo.eciComm.model.OrderItem;
import com.aygo.eciComm.model.Product;
import com.aygo.eciComm.model.enums.OrderStatus;
import com.aygo.eciComm.repository.OrderRepository;

@Service
public class OrderService {
	
	private static final Logger LOG = LogManager.getLogger(Order.class);
	
    private final OrderRepository orderRepository;
    private final ProductService productService;

    public OrderService(OrderRepository orderRepository, ProductService productService) {
        this.orderRepository = orderRepository;
        this.productService = productService;
    }

    public Order createOrder(Order order) {
        LOG.info("Creating new order for user: {}", order.getUserId());
        validateOrder(order);
        calculateOrderTotals(order);
        order.setStatus(OrderStatus.PENDING);
        return orderRepository.save(order);
    }

    public Order getOrder(String orderId) {
        LOG.debug("Fetching order: {}", orderId);
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));
    }

    public List<Order> getUserOrders(String userId) {
        LOG.debug("Fetching orders for user: {}", userId);
        return orderRepository.findByUserId(userId);
    }

    public Order updateOrderStatus(String orderId, OrderStatus newStatus) {
        LOG.info("Updating order status: {} to {}", orderId, newStatus);
        Order order = getOrder(orderId);
        order.setStatus(newStatus);
        return orderRepository.save(order);
    }

    private void validateOrder(Order order) {
        List<String> errors = new ArrayList<>();

        if (order.getUserId() == null || order.getUserId().trim().isEmpty()) {
            errors.add("User ID is required");
        }
        if (order.getItems() == null || order.getItems().isEmpty()) {
            errors.add("Order must contain at least one item");
        }
        if (order.getShippingAddress() == null || order.getShippingAddress().trim().isEmpty()) {
            errors.add("Shipping address is required");
        }

        // Validate each order item
        if (order.getItems() != null) {
            order.getItems().forEach(item -> {
                if (item.getQuantity() == null || item.getQuantity() <= 0) {
                    errors.add("Item quantity must be greater than zero");
                }
                // Verify product exists and has sufficient stock
                try {
                    Product product = productService.getProduct(item.getProductId());
                    if (product.getStockQuantity() < item.getQuantity()) {
                        errors.add("Insufficient stock for product: " + product.getName());
                    }
                } catch (ProductNotFoundException e) {
                    errors.add("Product not found: " + item.getProductId());
                }
            });
        }

        if (!errors.isEmpty()) {
            throw new OrderValidationException(String.join(", ", errors));
        }
    }

    private void calculateOrderTotals(Order order) {
        if (order.getItems() != null) {
            BigDecimal total = BigDecimal.ZERO;
            for (OrderItem item : order.getItems()) {
                Product product = productService.getProduct(item.getProductId());
                item.setUnitPrice(product.getPrice());
                item.setProductName(product.getName());
                item.setSubtotal(product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
                total = total.add(item.getSubtotal());
            }
            order.setTotalAmount(total);
        }
    }
}