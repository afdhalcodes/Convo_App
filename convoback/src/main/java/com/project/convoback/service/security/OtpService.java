package com.project.convoback.service.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Locale;

@Service
public class OtpService {

    private final SecureRandom secureRandom = new SecureRandom();

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    public String generateOtp() {

        int number = secureRandom.nextInt(1_000_000);

        return String.format(Locale.ROOT, "%06d", number);
    }

    public String hashOtp(String otp) {

        return encoder.encode(otp);
    }

    public boolean verifyOtp(String otp, String otpHash) {

        return encoder.matches(otp, otpHash);
    }
}
