package com.hotel.repository;

import com.hotel.entity.Invoice;
import com.hotel.entity.Payment;
import com.hotel.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByInvoice(Invoice invoice);
    List<Payment> findByUser(User user);
    Optional<Payment> findByReferenceCode(String referenceCode);
}
