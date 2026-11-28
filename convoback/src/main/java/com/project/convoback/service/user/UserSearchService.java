package com.project.convoback.service.user;

import com.project.convoback.dto.user.UserSearchResponse;
import com.project.convoback.model.security.User;
import com.project.convoback.repository.UserRepo;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class UserSearchService {

    private final UserRepo userRepo;

    public UserSearchService(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    public List<UserSearchResponse> searchUsers(
            String query,
            Authentication authentication
    ) {

        if (query == null || query.trim().length() < 2) {
            return List.of();
        }

        String identifier = authentication.getName();

        User currentUser = userRepo
                .findByEmailOrUsername(identifier, identifier)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Authenticated user not found"
                        )
                );

        return userRepo.searchUsers(
                        query.trim(),
                        currentUser.getId(),
                        PageRequest.of(0, 20)
                )
                .stream()
                .map(user -> new UserSearchResponse(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail()
                ))
                .toList();
    }

}
