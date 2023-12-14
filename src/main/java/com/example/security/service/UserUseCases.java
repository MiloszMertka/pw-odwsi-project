package com.example.security.service;

import com.example.security.dto.RegisterUserDto;
import com.example.security.dto.UserDto;

import java.util.List;

public interface UserUseCases {

    void registerUser(RegisterUserDto registerUserDto);

    List<UserDto> getAllUsers();

}
