package com.shopsphere.Controller;

import com.shopsphere.Entity.Order;
import com.shopsphere.Service.OrderService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {

        this.orderService = orderService;
    }


    // =========================
    // GET USER'S ORDERS
    // =========================

    @GetMapping("/user/{userId}")
    public List<Order> getUserOrders(
            @PathVariable Long userId) {

        return orderService.getUserOrders(userId);
    }


    // =========================
    // GET ORDER BY ID
    // =========================

    @GetMapping("/{orderId}")
    public Order getOrderById(
            @PathVariable Long orderId) {

        return orderService.getOrderById(orderId);
    }


    // =========================
    // GET ALL ORDERS - ADMIN
    // =========================

    @GetMapping("/admin/all")
    public List<Order> getAllOrders() {

        return orderService.getAllOrders();
    }


    // =========================
    // UPDATE ORDER STATUS - ADMIN
    // =========================

    @PutMapping("/admin/{orderId}/status")
    public Order updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam String status) {

        return orderService.updateOrderStatus(
                orderId,
                status
        );
    }


    // =========================
    // CANCEL ORDER - ADMIN
    // =========================

    @PutMapping("/admin/{orderId}/cancel")
    public Order cancelOrder(
            @PathVariable Long orderId) {

        return orderService.cancelOrder(orderId);
    }


    // =========================
    // CANCEL ORDER - CUSTOMER
    // =========================

    @PutMapping("/user/{userId}/{orderId}/cancel")
    public Order cancelOrderByUser(
            @PathVariable Long userId,
            @PathVariable Long orderId) {

        return orderService.cancelOrderByUser(
                userId,
                orderId
        );
    }
}