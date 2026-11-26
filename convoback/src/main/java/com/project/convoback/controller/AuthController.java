package com.project.convoback.controller;

import com.project.convoback.dto.authentication.LoginRequest;
import com.project.convoback.dto.authentication.RegisterRequest;
import com.project.convoback.dto.authentication.ResendOtpRequest;
import com.project.convoback.dto.authentication.VerifyOtpRequest;
import com.project.convoback.service.security.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register/request-otp")
    public ResponseEntity<String> requestOtp(
            @Valid @RequestBody RegisterRequest request
    ) {

        String message = authService.requestOtp(request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(message);
    }

    @PostMapping("/register/verify-otp")
    public ResponseEntity<String> verifyOtp(
            @Valid @RequestBody VerifyOtpRequest request
    ) {

        String message = authService.verifyOtp(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(message);
    }

    @PostMapping("/register/resend-otp")
    public ResponseEntity<String> resendOtp(
            @Valid @RequestBody ResendOtpRequest request
    ) {

        String message = authService.resendOtp(request);

        return ResponseEntity
                .status(HttpStatus.OK)
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
