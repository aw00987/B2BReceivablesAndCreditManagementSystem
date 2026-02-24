package com.aw00987.rcms.security;

import com.aw00987.rcms.entity.User;
import com.aw00987.rcms.enums.UserStatus;
import com.aw00987.rcms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Spring Security用のユーザー詳細サービス。
 * データベースからユーザー情報を取得し、認証用のUserDetailsオブジェクトを構築します。
 */
@Service
@RequiredArgsConstructor
public class UserSecurityService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * ユーザー名に基づいてユーザー情報を読み込みます。
     * @param username ユーザー名
     * @return UserDetails 認証用ユーザー詳細
     * @throws RuntimeException ユーザーが見つからない場合、または無効な場合
     */
    @Override
    public UserDetails loadUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("ユーザーが見つかりません: " + username));
        
        // ユーザーが有効な状態かチェック
        if (user.getStatus() == UserStatus.DISABLED) {
            throw new RuntimeException("ユーザーは無効化されています: " + username);
        }
        
        // Spring SecurityのUserオブジェクトを構築して返却
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPasswordHash(),
                Collections.singletonList(new SimpleGrantedAuthority(
                        "ROLE_" + user.getRole().name() // SecurityConfigのhasRoleと一致させるためROLE_プレフィックスを追加
                ))
        );
    }
}
