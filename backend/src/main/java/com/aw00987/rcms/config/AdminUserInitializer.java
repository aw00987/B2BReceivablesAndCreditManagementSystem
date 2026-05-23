package com.aw00987.rcms.config;

import com.aw00987.rcms.entity.User;
import com.aw00987.rcms.enums.UserRole;
import com.aw00987.rcms.enums.UserStatus;
import com.aw00987.rcms.repository.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class AdminUserInitializer implements CommandLineRunner {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Value("${rcms.default-admin.username}")
    private String defaultUsername;

    @Value("${rcms.default-admin.password}")
    private String defaultPassword;

    @Override
    public void run(String... args) {
        if (!StringUtils.hasText(defaultUsername) || !StringUtils.hasText(defaultPassword)) {
            return;
        }

        if (userMapper.selectByUsername(defaultUsername).isPresent()) {
            return;
        }

        User user = new User();
        user.setUsername(defaultUsername);
        user.setPasswordHash(passwordEncoder.encode(defaultPassword));
        user.setRealName(defaultUsername);
        user.setRole(UserRole.ADMIN);
        user.setStatus(UserStatus.ENABLED);
        userMapper.insert(user);
    }
}
