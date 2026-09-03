package com.shopsphere.Service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    @Value("${razorpay.key.id}")
    private String keyId;

    @Value("${razorpay.key.secret}")
    private String keySecret;


    // =========================
    // CREATE RAZORPAY ORDER
    // =========================

    public String createPaymentOrder(Long amount) throws Exception {

        RazorpayClient razorpayClient =
                new RazorpayClient(keyId, keySecret);

        JSONObject orderRequest = new JSONObject();

        orderRequest.put("amount", amount);
        orderRequest.put("currency", "INR");
        orderRequest.put(
                "receipt",
                "shopsphere_" + System.currentTimeMillis()
        );

        Order order =
                razorpayClient.orders.create(orderRequest);

        return order.toString();
    }


    // =========================
    // VERIFY PAYMENT
    // =========================

    public boolean verifyPayment(
            String razorpayOrderId,
            String razorpayPaymentId,
            String razorpaySignature
    ) throws Exception {

        JSONObject options = new JSONObject();

        options.put(
                "razorpay_order_id",
                razorpayOrderId
        );

        options.put(
                "razorpay_payment_id",
                razorpayPaymentId
        );

        options.put(
                "razorpay_signature",
                razorpaySignature
        );

        return Utils.verifyPaymentSignature(
                options,
                keySecret
        );
    }
}