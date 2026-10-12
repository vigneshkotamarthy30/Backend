package com.hotel.controller;

import com.hotel.entity.Invoice;
import com.hotel.entity.User;
import com.hotel.service.InvoiceService;
import com.hotel.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class InvoiceController {
    private final InvoiceService invoiceService;
    private final UserService userService;

    public InvoiceController(InvoiceService invoiceService, UserService userService) {
        this.invoiceService = invoiceService;
        this.userService = userService;
    }

    @GetMapping("/invoices/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Invoice>> getMyInvoices() {
        User currentUser = userService.getCurrentUser();
        return ResponseEntity.ok(invoiceService.getInvoicesForUser(currentUser));
    }

    @GetMapping("/invoices/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Invoice> getInvoice(@PathVariable Long id) {
        User currentUser = userService.getCurrentUser();
        Invoice invoice = invoiceService.getInvoiceById(id);
        if (!invoice.getUser().getId().equals(currentUser.getId()) && currentUser.getRole() != com.hotel.entity.Role.ADMIN) {
            throw new SecurityException("Not allowed to access this invoice.");
        }
        return ResponseEntity.ok(invoice);
    }
}
