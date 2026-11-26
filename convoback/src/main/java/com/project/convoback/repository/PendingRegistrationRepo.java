
package com.project.convoback.repository;

import com.project.convoback.model.security.PendingRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PendingRegistrationRepo extends JpaRepository<PendingRegistration, Long> {

    Optional<PendingRegistration> findByEmailIgnoreCase(String email);

    boolean existsByUsernameIgnoreCase(String username);

    void deleteByEmailIgnoreCase(String email);
}
