package com.hotel.service;

import com.hotel.entity.*;
import com.hotel.exception.ConflictException;
import com.hotel.exception.NotFoundException;
import com.hotel.exception.ValidationException;
import com.hotel.repository.InvoiceRepository;
import com.hotel.repository.PaymentRepository;
import com.hotel.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final UserRepository userRepository;

    public PaymentService(PaymentRepository paymentRepository, InvoiceRepository invoiceRepository, UserRepository userRepository) {
        this.paymentRepository = paymentRepository;
        this.invoiceRepository = invoiceRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Payment recordPayment(Long invoiceId, Long userId, BigDecimal amount, String method, String notes) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
            .orElseThrow(() -> new NotFoundException("Invoice not found."));
        if (!invoice.getUser().getId().equals(userId)) {
            throw new SecurityException("You cannot pay for another customer's invoice.");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Payment amount must be positive.");
        }
        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new ConflictException("This invoice is already fully paid.");
        }

        BigDecimal totalPaid = paymentRepository.findByInvoice(invoice).stream()
            .filter(p -> p.getStatus() == PaymentStatus.COMPLETED)
            .map(Payment::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal remaining = invoice.getTotalAmount().subtract(totalPaid);
        if (amount.compareTo(remaining) > 0) {
            throw new ValidationException("Payment exceeds remaining invoice balance.");
        }

        String referenceCode = "PMT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Payment payment = new Payment();
        payment.setInvoice(invoice);
        payment.setBooking(invoice.getBooking());
        payment.setUser(invoice.getUser());
        payment.setAmount(amount);
        payment.setMethod(method);
        payment.setReferenceCode(referenceCode);
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setNotes(notes);
        Payment saved = paymentRepository.save(payment);

        BigDecimal newTotalPaid = totalPaid.add(amount);
        invoice.setStatus(newTotalPaid.compareTo(invoice.getTotalAmount()) >= 0 ? InvoiceStatus.PAID : InvoiceStatus.PARTIALLY_PAID);
        invoiceRepository.save(invoice);
        return saved;
    }

    public List<Payment> getPaymentsForUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found."));
        return paymentRepository.findByUser(user);
    }
}
