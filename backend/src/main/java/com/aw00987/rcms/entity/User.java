package com.aw00987.rcms.entity;

import com.aw00987.rcms.enums.UserRole;
import com.aw00987.rcms.enums.UserStatus;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;


@Data
public class User {

    private Long id;

    private String username;

    private String passwordHash;

    private String realName;

    private UserRole role;

    private UserStatus status;//todo：调整为EnumType.STRING

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
