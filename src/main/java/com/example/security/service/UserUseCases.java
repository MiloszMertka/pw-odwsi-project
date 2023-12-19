package com.example.security.service;

import com.example.security.dto.LastSuccessfulLoginDto;
import com.example.security.dto.RegisterUserDto;
import com.example.security.dto.UserDto;
import com.example.security.model.User;

import java.util.List;

public interface UserUseCases {

    void registerUser(RegisterUserDto registerUserDto);

    List<UserDto> getAllOtherUsers(User user);

    List<LastSuccessfulLoginDto> getLastSuccessfulLogins(User user);

}
