package com.ajith.codejudge.user.repository;

import com.ajith.codejudge.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByVerificationToken(String token);

    Optional<User> findByResetToken(String token);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
