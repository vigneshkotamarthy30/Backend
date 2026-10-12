package com.hotel.repository;

import com.hotel.entity.Invoice;
import com.hotel.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    List<Invoice> findByUser(User user);
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
    Optional<Invoice> findByBookingId(Long bookingId);
}
