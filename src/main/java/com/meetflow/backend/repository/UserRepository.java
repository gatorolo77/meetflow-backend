package com.meetflow.backend.repository;

import com.meetflow.backend.model.User;
import com.meetflow.backend.model.UserRole;
import com.meetflow.backend.model.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    List<User> findByRoleAndStatus(UserRole role, UserStatus status);
}
