package com.example.whostolemyfood.user.presentation.controller;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.example.whostolemyfood.user.application.service.UserServiceV1;
import com.example.whostolemyfood.user.domain.entity.UserEntity;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserControllerV1 {

    private final UserServiceV1 userServiceV1;

}
