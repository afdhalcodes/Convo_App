package com.project.convoback.service.security;

import com.project.convoback.dto.authentication.LoginRequest;
import com.project.convoback.dto.authentication.RegisterRequest;
import com.project.convoback.dto.authentication.ResendOtpRequest;
import com.project.convoback.dto.authentication.VerifyOtpRequest;
import com.project.convoback.exception.EmailAlreadyExistsException;
import com.project.convoback.exception.InvalidCredentialsException;
import com.project.convoback.exception.UsernameAlreadyExistsException;
import com.project.convoback.model.security.PendingRegistration;
import com.project.convoback.model.security.User;
import com.project.convoback.repository.PendingRegistrationRepo;
import com.project.convoback.repository.UserRepo;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AuthService {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    private final UserRepo userRepo;

    private final PendingRegistrationRepo pendingRepo;

    private final AuthenticationManager authManager;

    private final JWTService JService;

    private final OtpService otpService;

    private final EmailService emailService;

    @Value("${app.otp.expiry-minutes:5}")
    private int otpExpiryMinutes;

    @Value("${app.otp.max-attempts:5}")
    private int maxOtpAttempts;

    public AuthService(
            UserRepo userRepo,
            PendingRegistrationRepo pendingRepo,
            AuthenticationManager authManager,
            JWTService jwtService,
            OtpService otpService,
            EmailService emailService
    ) {
        this.userRepo = userRepo;
        this.pendingRepo = pendingRepo;
        this.authManager = authManager;
        this.JService = jwtService;
        this.otpService = otpService;
        this.emailService = emailService;
    }

    @Transactional
    public String requestOtp(RegisterRequest request) {

        String email = request.getEmail().trim().toLowerCase();
        String username = request.getUsername().trim();

        if (userRepo.existsByEmailIgnoreCase(email)) {
            throw new EmailAlreadyExistsException(
                    "Email is already registered"
            );
        }

        if (userRepo.existsByUsernameIgnoreCase(username)) {
            throw new UsernameAlreadyExistsException(
                    "Username already exists"
            );
        }

        // Remove an older pending registration for this email
        pendingRepo.deleteByEmailIgnoreCase(email);

        // Check whether another pending registration uses this username
        if (pendingRepo.existsByUsernameIgnoreCase(username)) {
            throw new UsernameAlreadyExistsException(
                    "Username is currently being used for another registration"
            );
        }

        String otp = otpService.generateOtp();

        PendingRegistration pending = new PendingRegistration();

        pending.setUsername(username);
        pending.setEmail(email);

        // Encode password before temporary storage
        pending.setPassword(
                encoder.encode(request.getPassword())
        );

        // Store only the OTP hash
        pending.setOtpHash(
                otpService.hashOtp(otp)
        );

        pending.setExpiryTime(
                LocalDateTime.now().plusMinutes(otpExpiryMinutes)
        );

        pending.setAttempts(0);

        pending.setLastOtpSentAt(LocalDateTime.now());

        pendingRepo.save(pending);

        // Send original OTP to the user's email
        emailService.sendOtpEmail(email, otp);

        return "Verification OTP sent successfully to your email";
    }

    @Transactional(noRollbackFor = ResponseStatusException.class)
    public String verifyOtp(VerifyOtpRequest request) {

        String email = request.getEmail().trim().toLowerCase();

        PendingRegistration pending =
                pendingRepo.findByEmailIgnoreCase(email)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "No pending registration found for this email"
                                )
                        );

        // Check OTP expiration
        if (LocalDateTime.now().isAfter(pending.getExpiryTime())) {

            pendingRepo.delete(pending);

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "OTP has expired. Please register again."
            );
        }

        // Check maximum attempts
        if (pending.getAttempts() >= maxOtpAttempts) {

            pendingRepo.delete(pending);

            throw new ResponseStatusException(
                    HttpStatus.TOO_MANY_REQUESTS,
                    "Maximum OTP attempts exceeded. Please register again."
            );
        }

        // Check whether submitted OTP matches
        if (!otpService.verifyOtp(
                request.getOtp(),
                pending.getOtpHash()
        )) {

            pending.setAttempts(
                    pending.getAttempts() + 1
            );

            if (pending.getAttempts() >= maxOtpAttempts) {

                pendingRepo.delete(pending);

                throw new ResponseStatusException(
                        HttpStatus.TOO_MANY_REQUESTS,
                        "Maximum OTP attempts exceeded. Please register again."
                );
            }

            pendingRepo.save(pending);

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid OTP. Please try again."
            );
        }

        // Check again before creating the user
        if (userRepo.existsByEmailIgnoreCase(email)) {
            pendingRepo.delete(pending);

            throw new EmailAlreadyExistsException(
                    "Email is already registered"
            );
        }

        if (userRepo.existsByUsernameIgnoreCase(
                pending.getUsername()
        )) {
            pendingRepo.delete(pending);

            throw new UsernameAlreadyExistsException(
                    "Username already exists"
            );
        }

        // Create the actual user only after successful OTP verification
        User user = new User();

        user.setUsername(pending.getUsername());
        user.setEmail(pending.getEmail());

        // Password is already BCrypt encoded
        // Do not encode it again
        user.setPassword(pending.getPassword());

        userRepo.save(user);

        // Remove temporary registration
        pendingRepo.delete(pending);

        return "Email verified and user registered successfully";
    }

    @Transactional
    public String resendOtp(ResendOtpRequest request) {

        String email = request.getEmail().trim().toLowerCase();

        PendingRegistration pending =
                pendingRepo.findByEmailIgnoreCase(email)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "No pending registration found for this email"
                                )
                        );

        if (pending.getLastOtpSentAt() != null &&
                pending.getLastOtpSentAt().isAfter(
                        LocalDateTime.now().minusSeconds(60)
                )) {

            throw new ResponseStatusException(
                    HttpStatus.TOO_MANY_REQUESTS,
                    "Please wait 60 seconds before requesting another OTP"
            );
        }

        String newOtp = otpService.generateOtp();

        pending.setOtpHash(
                otpService.hashOtp(newOtp)
        );

        pending.setExpiryTime(
                LocalDateTime.now().plusMinutes(otpExpiryMinutes)
        );

        // Reset attempts for the new OTP
        pending.setAttempts(0);

        pending.setLastOtpSentAt(LocalDateTime.now());

        pendingRepo.save(pending);

        emailService.sendOtpEmail(email, newOtp);

        return "A new verification OTP has been sent";
    }

    public String verify(LoginRequest request) {

        try {

            Authentication authentication =
                    authManager.authenticate(
                            new UsernamePasswordAuthenticationToken(
                                    request.getIdentifier(),
                                    request.getPassword()
                            )
                    );

            if (!authentication.isAuthenticated()) {
                throw new InvalidCredentialsException(
                        "Invalid username or password"
                );
            }

            Optional<User> dbUser =
                    userRepo.findByEmailOrUsername(
                            request.getIdentifier(),request.getIdentifier()
                    );

            User user = dbUser.orElseThrow(() ->
                    new InvalidCredentialsException("Invalid username or password")
            );

            return JService.generateToken(user);

        } catch (InvalidCredentialsException exception) {

            throw exception;

        } catch (Exception exception) {

            throw new InvalidCredentialsException(
                    "Invalid username or password"
            );
        }
    }


}
