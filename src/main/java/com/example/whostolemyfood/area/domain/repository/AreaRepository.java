package com.example.whostolemyfood.area.domain.repository;

import com.example.whostolemyfood.area.domain.entity.AreaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AreaRepository extends JpaRepository<AreaEntity, UUID> {
}