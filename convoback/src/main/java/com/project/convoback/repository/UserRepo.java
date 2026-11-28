package com.project.convoback.repository;

import com.project.convoback.model.security.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<User,Long> {

    Optional<User> findByEmailOrUsername(String email,String username);

    Optional<User> findByEmailIgnoreCaseOrUsernameIgnoreCase(String email,String username);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByUsernameIgnoreCase(String username);

    @Query("""
            SELECT u FROM User u
            WHERE u.id <> :currentUserId
            AND (
                LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%'))
                OR LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%'))
            )
            ORDER BY u.username ASC
            """)
    List<User> searchUsers(
            @Param("query") String query,
            @Param("currentUserId") Long currentUserId,
            Pageable pageable
    );
}
