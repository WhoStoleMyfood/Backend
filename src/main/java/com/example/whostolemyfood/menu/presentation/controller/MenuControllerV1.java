package com.example.whostolemyfood.menu.presentation.controller;

import com.example.whostolemyfood.menu.application.service.MenuServiceV1;
import com.example.whostolemyfood.menu.presentation.dto.response.ResCreateMenuDtoV1;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.awt.*;

@RestController("/api/store/{id}/")
@RequiredArgsConstructor
public class MenuControllerV1 {

    private final MenuServiceV1 menuServiceV1;

    @Operation(summary = "메뉴 생성")
    @PostMapping
    public ResponseEntity<ResCreateMenuDtoV1> addMenu(@Valid @RequestBody ResCreateMenuDtoV1 request) {
        ResCreateMenuDtoV1 response = menuServiceV1.addMenu(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
