package com.hotel.controller;

import com.hotel.entity.Payment;
import com.hotel.entity.User;
import com.hotel.service.PaymentService;
import com.hotel.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class PaymentController {
    private final PaymentService paymentService;
    private final UserService userService;

    public PaymentController(PaymentService paymentService, UserService userService) {
        this.paymentService = paymentService;
        this.userService = userService;
    }

    @GetMapping("/payments/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Payment>> getMyPayments() {
        User currentUser = userService.getCurrentUser();
        return ResponseEntity.ok(paymentService.getPaymentsForUser(currentUser.getId()));
    }

    @PostMapping("/payments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Payment> submitPayment(@RequestBody Map<String, Object> payload) {
        Long invoiceId = Long.parseLong(payload.get("invoiceId").toString());
        BigDecimal amount = new BigDecimal(payload.get("amount").toString());
        String method = payload.get("method").toString();
        User currentUser = userService.getCurrentUser();
        return ResponseEntity.ok(paymentService.recordPayment(invoiceId, currentUser.getId(), amount, method, payload.getOrDefault("notes", "").toString()));
    }
}
