package com.shopsphere.Controller;

import com.shopsphere.DTO.PaymentVerifyRequest;
import com.shopsphere.Service.OrderService;
import com.shopsphere.Service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
@CrossOrigin(origins = "*")
public class PaymentController {

    private final PaymentService paymentService;
    private final OrderService orderService;

    public PaymentController(
            PaymentService paymentService,
            OrderService orderService
    ) {
        this.paymentService = paymentService;
        this.orderService = orderService;
    }

    // =========================
    // CREATE RAZORPAY ORDER
    // =========================
    @PostMapping("/create-order")
    public ResponseEntity<?> createPaymentOrder(
            @RequestParam Long userId
    ) {

        try {

            // Calculate cart total on the SERVER
            double cartTotal =
                    orderService.calculateCartTotal(userId);

            // Convert rupees to paise
            long amountInPaise =
                    Math.round(cartTotal * 100);

            String order =
                    paymentService.createPaymentOrder(
                            amountInPaise
                    );

            return ResponseEntity.ok(order);

        } catch (Exception e) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            "Unable to create payment order: "
                                    + e.getMessage()
                    );
        }
    }

    // =========================
    // VERIFY PAYMENT
    // =========================

    @PostMapping("/verify")
    public ResponseEntity<?> verifyPayment(
            @RequestBody PaymentVerifyRequest request,
            @RequestParam Long userId
    ) {

        try {

            boolean verified =
                    paymentService.verifyPayment(
                            request.getRazorpayOrderId(),
                            request.getRazorpayPaymentId(),
                            request.getRazorpaySignature()
                    );

            if (!verified) {

                return ResponseEntity
                        .badRequest()
                        .body("Payment verification failed.");
            }

            // =========================
            // PAYMENT VERIFIED
            // CREATE SHOPSPHERE ORDER
            // =========================

            var order =
                    orderService.checkout(
                            userId,
                            request.getRazorpayOrderId(),
                            request.getRazorpayPaymentId()
                    );

            return ResponseEntity.ok(order);

        } catch (Exception e) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            "Payment verification failed: "
                                    + e.getMessage()
                    );
        }
    }
}