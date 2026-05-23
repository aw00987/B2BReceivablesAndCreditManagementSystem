package com.aw00987.rcms.controller;

import com.aw00987.rcms.dto.LoginRequestDto;
import com.aw00987.rcms.dto.LoginResponseDto;
import com.aw00987.rcms.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto loginRequestDto) {

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                loginRequestDto.getUsername(), loginRequestDto.getPassword()
        ));

        final UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequestDto.getUsername());

        final String token = jwtUtil.generateToken(userDetails);

        String role = userDetails.getAuthorities().iterator().next().getAuthority()
                .replace("ROLE_", "");

        String username = userDetails.getUsername();

        LoginResponseDto loginResponseDto = LoginResponseDto.builder()
                .token(token).username(username).role(role)
                .build();

        return ResponseEntity.ok(loginResponseDto);
    }
}
