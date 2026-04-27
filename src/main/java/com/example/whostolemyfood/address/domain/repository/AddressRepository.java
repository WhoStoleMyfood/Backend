package com.example.whostolemyfood.address.domain.repository;

import com.example.whostolemyfood.address.domain.entity.AddressEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface AddressRepository extends JpaRepository<AddressEntity, UUID> {
    
    Page<AddressEntity> findAllByUserIdAndAliasContainingAndIsDeletedFalse(@Param("userId") UUID userId, @Param("alias") String alias, @Param("pageable") Pageable pageable);

    Page<AddressEntity> findAllByUserIdAndIsDeletedFalse(@Param("userId") UUID userId, @Param("pageable") Pageable pageable);

    Optional<AddressEntity> findByIdAndIsDeletedFalse(@Param("id") UUID id);

    Optional<AddressEntity> findByUserIdAndIsDefaultTrueAndIsDeletedFalse(@Param("userId") UUID userId);
}
