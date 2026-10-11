package com.agri.query.service;

import com.agri.query.dto.ForgotPasswordRequest;
import com.agri.query.dto.PasswordResetRequest;
import com.agri.query.dto.RegisterRequest;
import com.agri.query.dto.UserDto;
import com.agri.query.entity.User;

import java.util.Optional;

public interface AuthService {
    UserDto register(RegisterRequest request);
    void forgotPassword(ForgotPasswordRequest request);
    void resetPassword(PasswordResetRequest request);
    Optional<User> getCurrentAuthenticatedUser();
    UserDto getCurrentUserDto();
}
