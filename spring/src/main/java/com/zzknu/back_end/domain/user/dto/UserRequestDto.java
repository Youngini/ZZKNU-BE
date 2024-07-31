package com.zzknu.back_end.domain.user.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.Date;

@Builder
@Getter
public class UserRequestDto {
    private String email;
    private String password;
    private String nickname;
    private Date birthdate;
}
