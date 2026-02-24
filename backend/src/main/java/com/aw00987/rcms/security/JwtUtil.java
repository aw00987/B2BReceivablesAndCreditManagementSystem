package com.aw00987.rcms.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT（JSON Web Token）の生成、解析、検証を行うユーティリティクラス。
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    /**
     * 署名用の秘密鍵を取得します。
     * @return 秘密鍵
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * ユーザー詳細情報に基づいてトークンを生成します。
     * @param userDetails ユーザー詳細
     * @return 生成されたJWTトークン
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername());
    }

    /**
     * クレームとサブジェクトに基づいてトークンを作成します。
     * @param claims クレーム
     * @param subject サブジェクト（通常はユーザー名）
     * @return JWTトークン
     */
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * トークンが有効かどうかを検証します。
     * @param token JWTトークン
     * @param userDetails ユーザー詳細
     * @return 有効な場合はtrue、それ以外はfalse
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    /**
     * トークンからユーザー名を抽出します。
     * @param token JWTトークン
     * @return ユーザー名
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * トークンから有効期限を抽出します。
     * @param token JWTトークン
     * @return 有効期限
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * トークンから特定のクレームを抽出します。
     * @param token JWTトークン
     * @param claimsResolver クレーム解決関数
     * @param <T> クレームの型
     * @return 抽出されたクレーム
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * トークンからすべてのクレームを抽出します。
     * @param token JWTトークン
     * @return すべてのクレーム
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * トークンが期限切れかどうかを確認します。
     * @param token JWTトークン
     * @return 期限切れの場合はtrue、それ以外はfalse
     */
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
}
