package com.project.convoback.service.security;

import com.project.convoback.model.security.User;
import com.project.convoback.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MyUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepo userRepo;

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        User user = userRepo
                .findByEmailOrUsername(identifier,identifier)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "User not found: " + identifier
                        )
                );

        return new UserPrincipal(user);
    }
}
