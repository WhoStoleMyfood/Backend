package com.example.whostolemyfood.menu.application.service;

import com.example.whostolemyfood.menu.domain.repository.MenuRepository;
import com.example.whostolemyfood.menu.presentation.dto.request.ReqCreateMenuDtoV1;
import com.example.whostolemyfood.menu.presentation.dto.response.ResCreateMenuDtoV1;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MenuServiceV1 {

    private final MenuRepository menuRepository;

    public ResCreateMenuDtoV1 addMenu(ReqCreateMenuDtoV1 request) {

    }
}
