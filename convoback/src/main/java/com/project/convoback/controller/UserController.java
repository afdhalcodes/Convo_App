package com.project.convoback.controller;

import com.project.convoback.dto.user.UserSearchResponse;
import com.project.convoback.service.user.UserSearchService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserSearchService userSearchService;

    public UserController(UserSearchService userSearchService) {
        this.userSearchService = userSearchService;
    }

    @GetMapping("/search")
    public List<UserSearchResponse> searchUsers(
            @RequestParam("q") String query,
            Authentication authentication
    ) {
        return userSearchService.searchUsers(
                query,
                authentication
        );
    }

}
