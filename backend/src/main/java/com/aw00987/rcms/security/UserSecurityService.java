package com.aw00987.rcms.security;

import com.aw00987.rcms.entity.User;
import com.aw00987.rcms.enums.UserStatus;
import com.aw00987.rcms.repository.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Spring Security用のユーザー詳細サービス。
 * データベースからユーザー情報を取得し、認証用のUserDetailsオブジェクトを構築します。
 */
@Service
@RequiredArgsConstructor
public class UserSecurityService implements UserDetailsService {

    private final UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userMapper.selectByUsername(username).orElseThrow(
                () -> new UsernameNotFoundException("ユーザーが見つかりません: " + username)
        );

        // ユーザーが有効な状態かチェック
        if (user.getStatus() == UserStatus.DISABLED) {
            throw new UsernameNotFoundException("ユーザーが無効です: " + username);
        }

        // Spring SecurityのUserオブジェクトを構築して返却
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(), user.getPasswordHash(),
                Collections.singletonList(new SimpleGrantedAuthority(
                        "ROLE_" + user.getRole().name() // SecurityConfigのhasRoleと一致させるためROLE_プレフィックスを追加
                ))
        );
    }
}
