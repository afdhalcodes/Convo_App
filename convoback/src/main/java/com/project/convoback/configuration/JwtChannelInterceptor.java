package com.project.convoback.configuration;

import com.project.convoback.service.security.JWTService;
import com.project.convoback.service.security.MyUserDetailsService;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.stereotype.Component;

@Component
public class JwtChannelInterceptor implements ChannelInterceptor {

    private final JWTService jwtService;
    private final MyUserDetailsService userDetailsService;

    public JwtChannelInterceptor(
            JWTService jwtService,
            MyUserDetailsService userDetailsService
    ) {

        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public Message<?> preSend(
            Message<?> message,
            MessageChannel channel
    ) {

        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(
                        message,
                        StompHeaderAccessor.class
                );

        if (accessor == null) {
            return message;
        }

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {

            String authorization =
                    accessor.getFirstNativeHeader("Authorization");

            if (authorization == null ||
                    !authorization.startsWith("Bearer ")) {

                throw new IllegalArgumentException(
                        "Missing or invalid Authorization header"
                );
            }

            String token = authorization.substring(7);

            String username = jwtService.extractUsername(token);

            UserDetails userDetails =
                    userDetailsService.loadUserByUsername(username);

            if (!jwtService.validate(token, userDetails)) {

                throw new IllegalArgumentException(
                        "Invalid JWT token"
                );
            }

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

            accessor.setUser(authentication);
        }

        return message;
    }
}
