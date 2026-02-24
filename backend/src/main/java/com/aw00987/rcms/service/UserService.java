package com.aw00987.rcms.service;

import com.aw00987.rcms.dto.UserRequestDto;
import com.aw00987.rcms.dto.UserResponseDto;
import com.aw00987.rcms.entity.User;
import com.aw00987.rcms.enums.UserRole;
import com.aw00987.rcms.enums.UserStatus;
import com.aw00987.rcms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ユーザー管理に関連するビジネスロジックを提供するサービス。
 * ユーザーの作成、取得、無効化などの操作を担当します。
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * 新しいユーザーを作成します。
     * @param userRequestDto ユーザー作成リクエスト情報
     * @return 保存されたユーザーエンティティ
     */
    @Transactional
    public User createUser(UserRequestDto userRequestDto) {
        User user = new User();
        user.setUsername(userRequestDto.getUsername());
        user.setPasswordHash(userRequestDto.getPassword());
        user.setRealName(userRequestDto.getRealName());
        user.setRole(UserRole.valueOf(userRequestDto.getRole()));
        user.setStatus(UserStatus.ENABLED);
        return userRepository.save(user);
    }

    /**
     * 有効なユーザーのリストをページングして取得します。
     * @param pageable ページング情報
     * @return ユーザーレスポンスDTOのページ
     */
    public Page<UserResponseDto> getUsers(Pageable pageable) {
        return userRepository.findByStatus(UserStatus.ENABLED, pageable).map(this::mapToUserResponseDTO);
    }

    /**
     * 指定されたユーザー名のユーザーを無効化します。
     * @param username 無効化するユーザーのユーザー名
     */
    @Transactional
    public void disableUser(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            user.setStatus(UserStatus.DISABLED);
            userRepository.save(user);
        });
    }

    /**
     * ユーザーエンティティをレスポンスDTOに変換します。
     * @param user ユーザーエンティティ
     * @return ユーザーレスポンスDTO
     */
    private UserResponseDto mapToUserResponseDTO(User user) {
        UserResponseDto dto = new UserResponseDto();
        dto.setUsername(user.getUsername());
        dto.setRealName(user.getRealName());
        dto.setRole(user.getRole().getLabel());
        return dto;
    }
}
