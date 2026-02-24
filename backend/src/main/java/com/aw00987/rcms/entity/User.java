package com.aw00987.rcms.entity;

import com.aw00987.rcms.enums.UserRole;
import com.aw00987.rcms.enums.UserStatus;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * ユーザー
 */
@Data
@Entity
@Table(name = "users")
public class User {

    /**
     * ID
     * primary key
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * ユーザー名
     */
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    /**
     * パスワードハッシュ
     */
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    /**
     * 氏名
     */
    @Column(name = "real_name", nullable = false, length = 100)
    private String realName;

    /**
     * 役割
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    /**
     * ステータス
     * 1は有効 0は無効
     */
    @Enumerated(EnumType.ORDINAL)//todo：调整为EnumType.STRING
    @Column(nullable = false)
    private UserStatus status;

    /**
     * 作成日時
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新日時
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
