package com.hotel.service;

import com.hotel.entity.*;
import com.hotel.exception.NotFoundException;
import com.hotel.repository.InvoiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class InvoiceService {
    private final InvoiceRepository invoiceRepository;

    public InvoiceService(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    @Transactional
    public Invoice createInvoiceForBooking(Booking booking) {
        Invoice existing = invoiceRepository.findByBookingId(booking.getId()).orElse(null);
        if (existing != null) {
            return existing;
        }
        Invoice invoice = new Invoice();
        invoice.setBooking(booking);
        invoice.setUser(booking.getUser());
        invoice.setSubtotal(booking.getTotalAmount());
        invoice.setTaxAmount(BigDecimal.ZERO);
        invoice.setAdditionalCharges(BigDecimal.ZERO);
        invoice.setTotalAmount(booking.getTotalAmount());
        invoice.setStatus(InvoiceStatus.UNPAID);
        invoice.setInvoiceNumber(generateInvoiceNumber());
        return invoiceRepository.save(invoice);
    }

    public List<Invoice> getInvoicesForUser(User user) {
        return invoiceRepository.findByUser(user);
    }

    public Invoice getInvoiceById(Long invoiceId) {
        return invoiceRepository.findById(invoiceId)
            .orElseThrow(() -> new NotFoundException("Invoice not found."));
    }

    private String generateInvoiceNumber() {
        return "INV-" + UUID.randomUUID().toString().substring(0, 10).toUpperCase();
    }
}
