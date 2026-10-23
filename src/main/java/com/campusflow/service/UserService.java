package com.campusflow.service;

import com.campusflow.dto.UserRegistrationDto;
import com.campusflow.entity.User;

public interface UserService {
    User registerUser(UserRegistrationDto registrationDto);
    User findByEmail(String email);
    User findById(Long id);
    boolean existsByEmail(String email);
}
