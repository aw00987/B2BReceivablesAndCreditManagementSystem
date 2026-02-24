package com.aw00987.rcms.service;

import com.aw00987.rcms.dto.LoginRequestDto;
import com.aw00987.rcms.dto.LoginResponseDto;
import com.aw00987.rcms.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

/**
 * 認証関連のビジネスロジックを提供するサービス。
 * ログイン処理とJWTトークンの発行を担当します。
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;

    /**
     * ユーザーのログイン認証を行い、成功した場合はJWTトークンを返します。
     * @param loginRequestDto ログインリクエスト情報（ユーザー名、パスワード）
     * @return LoginResponseDto ログインレスポンス情報（トークン、ユーザー名、ロール）
     */
    public LoginResponseDto login(LoginRequestDto loginRequestDto) {
        // 認証マネージャーによる認証実行
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequestDto.getUsername(), loginRequestDto.getPassword())
        );

        // ユーザー詳細の取得
        final UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequestDto.getUsername());
        // JWTトークンの生成
        final String token = jwtUtil.generateToken(userDetails);

        // レスポンスDTOの構築
        return LoginResponseDto.builder()
                .token(token)
                .username(userDetails.getUsername())
                .role(userDetails.getAuthorities().iterator().next().getAuthority().replace("ROLE_", ""))
                .build();
    }
}
