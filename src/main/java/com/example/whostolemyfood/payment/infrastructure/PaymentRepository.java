package com.example.whostolemyfood.payment.infrastructure;

import com.example.whostolemyfood.payment.domain.PaymentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity, UUID> {

    Page<PaymentEntity> findAllByCreatedBy(UUID userID, Pageable pageable);

    Optional<PaymentEntity> findAllByCreatedByAndId(UUID userID, UUID id);
}
