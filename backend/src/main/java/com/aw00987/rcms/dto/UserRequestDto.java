package com.aw00987.rcms.dto;

import lombok.Data;

@Data
public class UserRequestDto {
    private String username;
    private String password;
    private String realName;
    private String role;
}
