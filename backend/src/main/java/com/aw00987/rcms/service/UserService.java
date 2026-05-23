package com.aw00987.rcms.service;

import com.aw00987.rcms.dto.UserRequestDto;
import com.aw00987.rcms.dto.UserResponseDto;
import com.aw00987.rcms.entity.User;
import com.aw00987.rcms.enums.UserRole;
import com.aw00987.rcms.enums.UserStatus;
import com.aw00987.rcms.repository.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * todo: 增删改列表查
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;

    @Transactional
    public void createUser(UserRequestDto userRequestDto) {
        User user = new User();
        user.setUsername(userRequestDto.getUsername());
        user.setPasswordHash(userRequestDto.getPassword());
        user.setRealName(userRequestDto.getRealName());
        user.setRole(UserRole.valueOf(userRequestDto.getRole()));
        user.setStatus(UserStatus.ENABLED);
        userMapper.insert(user);
    }

    public Page<UserResponseDto> getUsers(Pageable pageable) {
        List<UserResponseDto> list = userMapper.selectPage(pageable.getOffset(), pageable.getPageSize());
        long count = userMapper.selectCount();
        return new PageImpl<>(list, pageable, count);
    }

    @Transactional
    public void disableUserById(Long id) {
        User user = new User();
        user.setId(id);
        user.setStatus(UserStatus.DISABLED);
        userMapper.update(user);
    }
}
