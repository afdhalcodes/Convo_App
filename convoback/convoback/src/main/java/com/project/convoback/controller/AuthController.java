package com.project.convoback.controller;

import com.project.convoback.dto.authentication.LoginRequest;
import com.project.convoback.dto.authentication.RegisterRequest;
import com.project.convoback.service.security.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<String> register(
            @Valid @RequestBody RegisterRequest request
    ) {

        String message =
                authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(message);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(
            @RequestBody LoginRequest request
    ) {

        String token =
                authService.verify(request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(token);
    }
}
