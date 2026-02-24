package com.aw00987.rcms.controller;

import com.aw00987.rcms.dto.UserRequestDto;
import com.aw00987.rcms.dto.UserResponseDto;
import com.aw00987.rcms.entity.User;
import com.aw00987.rcms.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody UserRequestDto userRequestDto) {
        return ResponseEntity.ok(userService.createUser(userRequestDto));
    }

    @GetMapping
    public ResponseEntity<Page<UserResponseDto>> getUsers(Pageable pageable) {
        return ResponseEntity.ok(userService.getUsers(pageable));
    }

    @DeleteMapping("/{username}")
    public ResponseEntity<Void> disableUser(@PathVariable String username) {
        userService.disableUser(username);
        return ResponseEntity.noContent().build();
    }
}
