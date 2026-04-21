package com.example.whostolemyfood.menu.domain.repository;

import com.example.whostolemyfood.menu.domain.entity.MenuEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuRepository extends JpaRepository<MenuEntity, Long> {
}
