package com.aw00987.rcms.repository;

import com.aw00987.rcms.entity.User;
import com.aw00987.rcms.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Page<User> findByStatus(UserStatus status, Pageable pageable);
}
