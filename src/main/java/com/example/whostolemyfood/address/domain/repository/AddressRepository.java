package com.example.whostolemyfood.address.domain.repository;

import com.example.whostolemyfood.address.domain.entity.AddressEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AddressRepository extends JpaRepository<AddressEntity, UUID> {
    
    Page<AddressEntity> findAllByUserIdAndAliasContainingAndIsDeletedFalse(UUID userId, String alias, Pageable pageable);

    Page<AddressEntity> findAllByUserIdAndIsDeletedFalse(UUID userId, Pageable pageable);

    Optional<AddressEntity> findByIdAndIsDeletedFalse(UUID id);

    Optional<AddressEntity> findByUserIdAndIsDefaultTrueAndIsDeletedFalse(UUID userId);
}
