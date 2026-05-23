package com.aw00987.rcms.service;

import com.aw00987.rcms.dto.UserRequestDto;
import com.aw00987.rcms.dto.UserResponseDto;
import com.aw00987.rcms.entity.User;
import com.aw00987.rcms.enums.UserRole;
import com.aw00987.rcms.enums.UserStatus;
import com.aw00987.rcms.repository.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    @Test
    void createUserMapsRequestToEnabledUserAndInserts() {
        UserRequestDto request = userRequest("tanaka", "secret", "Tanaka Taro", "ADMIN");

        userService.createUser(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).insert(userCaptor.capture());
        User inserted = userCaptor.getValue();
        assertThat(inserted.getId()).isNull();
        assertThat(inserted.getUsername()).isEqualTo("tanaka");
        assertThat(inserted.getPasswordHash()).isEqualTo("secret");
        assertThat(inserted.getRealName()).isEqualTo("Tanaka Taro");
        assertThat(inserted.getRole()).isEqualTo(UserRole.ADMIN);
        assertThat(inserted.getStatus()).isEqualTo(UserStatus.ENABLED);
    }

    @Test
    void createUserRejectsUnknownRoleBeforeInsert() {
        UserRequestDto request = userRequest("tanaka", "secret", "Tanaka Taro", "UNKNOWN");

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No enum constant");
        verifyNoInteractions(userMapper);
    }

    @Test
    void getUsersReturnsPageFromMapperContentAndCount() {
        Pageable pageable = PageRequest.of(2, 3);
        UserResponseDto first = userResponse("u1", "User One", "ADMIN");
        UserResponseDto second = userResponse("u2", "User Two", "SALES");
        when(userMapper.selectPage(6L, 3)).thenReturn(List.of(first, second));
        when(userMapper.selectCount()).thenReturn(11L);

        Page<UserResponseDto> result = userService.getUsers(pageable);

        assertThat(result.getContent()).containsExactly(first, second);
        assertThat(result.getNumber()).isEqualTo(2);
        assertThat(result.getSize()).isEqualTo(3);
        assertThat(result.getTotalElements()).isEqualTo(11);
        assertThat(result.getTotalPages()).isEqualTo(4);
        verify(userMapper).selectPage(6L, 3);
        verify(userMapper).selectCount();
    }

    @Test
    void getUsersSupportsEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        when(userMapper.selectPage(0L, 10)).thenReturn(List.of());
        when(userMapper.selectCount()).thenReturn(0L);

        Page<UserResponseDto> result = userService.getUsers(pageable);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
        assertThat(result.getTotalPages()).isZero();
        verify(userMapper).selectPage(0L, 10);
        verify(userMapper).selectCount();
    }

    @Test
    void disableUserByIdUpdatesOnlyIdAndDisabledStatus() {
        userService.disableUserById(42L);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).update(userCaptor.capture());
        User updated = userCaptor.getValue();
        assertThat(updated.getId()).isEqualTo(42L);
        assertThat(updated.getStatus()).isEqualTo(UserStatus.DISABLED);
        assertThat(updated.getUsername()).isNull();
        assertThat(updated.getPasswordHash()).isNull();
        assertThat(updated.getRealName()).isNull();
        assertThat(updated.getRole()).isNull();
    }

    private static UserRequestDto userRequest(String username, String password, String realName, String role) {
        UserRequestDto dto = new UserRequestDto();
        dto.setUsername(username);
        dto.setPassword(password);
        dto.setRealName(realName);
        dto.setRole(role);
        return dto;
    }

    private static UserResponseDto userResponse(String username, String realName, String role) {
        UserResponseDto dto = new UserResponseDto();
        dto.setUsername(username);
        dto.setRealName(realName);
        dto.setRole(role);
        return dto;
    }
}
