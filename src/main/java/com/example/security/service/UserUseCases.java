package com.example.security.service;

import com.example.security.dto.*;
import com.example.security.model.User;

import java.util.List;

public interface UserUseCases {

    void registerUser(RegisterUserDto registerUserDto);

    List<UserDto> getAllOtherUsers(User user);

    List<LastSuccessfulLoginDto> getLastSuccessfulLogins(User user);

    void createPasswordResetToken(CreatePasswordResetTokenDto createPasswordResetTokenDto);

    boolean isPasswordResetTokenValid(String token);

    void changePassword(ChangePasswordDto changePasswordDto);

}
