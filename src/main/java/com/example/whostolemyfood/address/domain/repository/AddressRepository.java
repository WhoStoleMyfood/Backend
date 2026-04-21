package com.example.whostolemyfood.address.domain.repository;

import com.example.whostolemyfood.address.domain.entity.AddressEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AddressRepository extends JpaRepository<AddressEntity, UUID> {
}