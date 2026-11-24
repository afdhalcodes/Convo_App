package com.project.convoback.service.security;

import com.project.convoback.dto.authentication.LoginRequest;
import com.project.convoback.dto.authentication.RegisterRequest;
import com.project.convoback.exception.EmailAlreadyExistsException;
import com.project.convoback.exception.InvalidCredentialsException;
import com.project.convoback.exception.UsernameAlreadyExistsException;
import com.project.convoback.model.security.User;
import com.project.convoback.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private AuthenticationManager authManager;

    @Autowired
    private JWTService JService;

    public String register(RegisterRequest request) {

        if (userRepo.existsByEmail(
                request.getEmail()
        )) {
            throw new EmailAlreadyExistsException(
                    "Email is already registered"
            );
        }

        if (userRepo.existsByUsername(
                request.getUsername()
        )) {
            throw new UsernameAlreadyExistsException(
                    "Username already exists"
            );
        }

        User user = new User();

        user.setUsername(
                request.getUsername()
        );

        user.setEmail(
                request.getEmail()
        );

        user.setPassword(
                encoder.encode(
                        request.getPassword()
                )
        );

        userRepo.save(user);

        return "User Registered Successfully";
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
