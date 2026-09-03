package com.shopsphere.Service;

import com.shopsphere.Entity.Cart;
import com.shopsphere.Entity.Order;
import com.shopsphere.Entity.OrderItem;
import com.shopsphere.Entity.Product;
import com.shopsphere.Entity.User;

import com.shopsphere.Repository.CartRepository;
import com.shopsphere.Repository.OrderRepository;
import com.shopsphere.Repository.ProductRepository;
import com.shopsphere.Repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;


    // =========================
    // CONSTRUCTOR
    // =========================

    public OrderService(
            OrderRepository orderRepository,
            UserRepository userRepository,
            CartRepository cartRepository,
            ProductRepository productRepository) {

        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
    }


    // =========================
    // CHECKOUT
    // =========================

    @Transactional
    public Order checkout(
            Long userId,
            String razorpayOrderId,
            String razorpayPaymentId
    ) {

        // Find user
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found"
                        )
                );


        // Get user's cart
        List<Cart> cartItems =
                cartRepository.findByUser(user);


        // Check empty cart
        if (cartItems.isEmpty()) {

            throw new RuntimeException(
                    "Cart is empty"
            );
        }


        // Create order
        Order order = new Order();

        order.setUser(user);

        order.setOrderDate(
                LocalDateTime.now()
        );

        order.setStatus("PLACED");
        order.setPaymentMethod("RAZORPAY");
        order.setPaymentStatus("PAID");
        order.setRazorpayOrderId(razorpayOrderId);
        order.setRazorpayPaymentId(razorpayPaymentId);


        double total = 0;


        List<OrderItem> orderItems =
                new ArrayList<>();


        // =========================
        // PROCESS CART ITEMS
        // =========================

        for (Cart cart : cartItems) {

            Product product =
                    cart.getProduct();

            int quantity =
                    cart.getQuantity();


            // =========================
            // STOCK VALIDATION
            // =========================

            if (product.getStock() <= 0) {

                throw new RuntimeException(
                        product.getName() +
                                " is out of stock"
                );
            }


            if (quantity > product.getStock()) {

                throw new RuntimeException(
                        "Only " +
                                product.getStock() +
                                " units available for " +
                                product.getName()
                );
            }


            // =========================
            // CALCULATE ITEM TOTAL
            // =========================

            double itemTotal =
                    product.getPrice() *
                            quantity;

            total += itemTotal;


            // =========================
            // CREATE ORDER ITEM
            // =========================

            OrderItem orderItem =
                    new OrderItem();

            orderItem.setOrder(order);

            orderItem.setProduct(product);

            orderItem.setQuantity(quantity);


            // Save current product price
            // at the time of purchase

            orderItem.setPrice(
                    product.getPrice()
            );


            orderItems.add(orderItem);


            // =========================
            // REDUCE STOCK
            // =========================

            product.setStock(
                    product.getStock() -
                            quantity
            );


            // Save updated product stock

            productRepository.save(product);

        }


        // =========================
        // SAVE ORDER DETAILS
        // =========================

        order.setTotalAmount(total);

        order.setItems(orderItems);


        // =========================
        // SAVE ORDER
        // =========================

        Order savedOrder =
                orderRepository.save(order);


        // =========================
        // CLEAR CART
        // =========================

        cartRepository.deleteAll(
                cartItems
        );


        // =========================
        // RETURN ORDER
        // =========================

        return savedOrder;
    }



    // =========================
    // GET USER ORDERS
    // =========================

    public List<Order> getUserOrders(
            Long userId) {

        User user =
                userRepository.findById(userId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "User not found"
                                )
                        );


        return orderRepository.findByUser(
                user
        );
    }


    // =========================
    // GET ORDER BY ID
    // =========================

    public Order getOrderById(
            Long orderId) {

        return orderRepository.findById(
                orderId
        ).orElseThrow(() ->
                new RuntimeException(
                        "Order not found"
                )
        );
    }

    // =========================
    // GET ALL ORDERS - ADMIN
    // =========================

    public List<Order> getAllOrders() {

        return orderRepository.findAll()
                .stream()
                .filter(order ->
                        order.getUser() != null &&
                                !"ADMIN".equals(order.getUser().getRole())
                )
                .toList();

    }

    // =========================
// UPDATE ORDER STATUS - ADMIN
// =========================

    public Order updateOrderStatus(
            Long orderId,
            String status) {

        Order order =
                orderRepository.findById(orderId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Order not found"
                                )
                        );

        order.setStatus(status);

        return orderRepository.save(order);
    }

    // =========================
// CANCEL ORDER - ADMIN
// =========================

    @Transactional
    public Order cancelOrder(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Order not found"
                        )
                );

        // Already cancelled
        if ("CANCELLED".equals(order.getStatus())) {

            throw new RuntimeException(
                    "Order is already cancelled"
            );
        }

        // Cannot cancel delivered order
        if ("DELIVERED".equals(order.getStatus())) {

            throw new RuntimeException(
                    "Delivered order cannot be cancelled"
            );
        }

        // Restore product stock
        for (OrderItem item : order.getItems()) {

            Product product = item.getProduct();

            product.setStock(
                    product.getStock() +
                            item.getQuantity()
            );

            productRepository.save(product);
        }

        // Change order status
        order.setStatus("CANCELLED");

        return orderRepository.save(order);
    }

    // =========================
// CANCEL ORDER - CUSTOMER
// =========================

    @Transactional
    public Order cancelOrderByUser(
            Long userId,
            Long orderId) {

        // Find user
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found"
                        )
                );

        // Find order
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Order not found"
                        )
                );

        // Make sure this order belongs to this user
        if (!order.getUser().getId().equals(user.getId())) {

            throw new RuntimeException(
                    "You are not allowed to cancel this order"
            );
        }

        // Already cancelled
        if ("CANCELLED".equals(order.getStatus())) {

            throw new RuntimeException(
                    "Order is already cancelled"
            );
        }

        // Customer can only cancel PLACED orders
        if (!"PLACED".equals(order.getStatus())) {

            throw new RuntimeException(
                    "Only placed orders can be cancelled"
            );
        }

        // Restore product stock
        for (OrderItem item : order.getItems()) {

            Product product = item.getProduct();

            product.setStock(
                    product.getStock() +
                            item.getQuantity()
            );

            productRepository.save(product);
        }

        // Change order status
        order.setStatus("CANCELLED");

        return orderRepository.save(order);
    }

    public double calculateCartTotal(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        List<Cart> cartItems = cartRepository.findByUser(user);

        if (cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        double total = 0;

        for (Cart cart : cartItems) {

            Product product = cart.getProduct();

            if (product.getStock() <= 0) {
                throw new RuntimeException(
                        product.getName() + " is out of stock"
                );
            }

            if (cart.getQuantity() > product.getStock()) {
                throw new RuntimeException(
                        "Only " + product.getStock()
                                + " units available for "
                                + product.getName()
                );
            }

            total += product.getPrice() * cart.getQuantity();
        }

        return total;
    }
}