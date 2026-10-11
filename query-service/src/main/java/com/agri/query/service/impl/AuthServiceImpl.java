package com.agri.query.service.impl;

import com.agri.query.dto.ForgotPasswordRequest;
import com.agri.query.dto.PasswordResetRequest;
import com.agri.query.dto.RegisterRequest;
import com.agri.query.dto.UserDto;
import com.agri.query.entity.FarmerProfile;
import com.agri.query.entity.PasswordResetToken;
import com.agri.query.entity.User;
import com.agri.query.repository.FarmerProfileRepository;
import com.agri.query.repository.PasswordResetTokenRepository;
import com.agri.query.repository.UserRepository;
import com.agri.query.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserRepository userRepository;
    private final FarmerProfileRepository farmerProfileRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(UserRepository userRepository,
                           FarmerProfileRepository farmerProfileRepository,
                           PasswordResetTokenRepository passwordResetTokenRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.farmerProfileRepository = farmerProfileRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public UserDto register(RegisterRequest request) {
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        String email = request.getEmail().trim().toLowerCase();
        if (!email.contains("@") || !email.contains(".")) {
            throw new IllegalArgumentException("A valid email address is required");
        }
        if (request.getPassword() == null || request.getPassword().length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters long");
        }
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("An account with email " + email + " already exists");
        }

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName() != null ? request.getFullName().trim() : "Farmer");
        user.setPhone(request.getPhone() != null ? request.getPhone().trim() : null);
        user.setRole("ROLE_FARMER");
        user.setEnabled(true);
        User savedUser = userRepository.save(user);

        // Auto-provision initial farmer profile
        FarmerProfile profile = new FarmerProfile();
        profile.setUser(savedUser);
        profile.setPreferredLanguage("en");
        profile.setTotalLandAcres(java.math.BigDecimal.ZERO);
        farmerProfileRepository.save(profile);

        logger.info("Successfully registered farmer user with email: {}", email);
        return toDto(savedUser);
    }

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        String email = request.getEmail().trim().toLowerCase();
        Optional<User> userOpt = userRepository.findByEmailIgnoreCase(email);
        if (userOpt.isEmpty()) {
            logger.info("Password reset requested for non-existing email: {}", email);
            return; // Timing-safe early exit
        }

        User user = userOpt.get();
        String rawToken = UUID.randomUUID().toString().replace("-", "");
        String tokenHash = hashToken(rawToken);

        PasswordResetToken resetToken = new PasswordResetToken(user, tokenHash, LocalDateTime.now().plusMinutes(15));
        passwordResetTokenRepository.save(resetToken);

        logger.info("Password reset generated for {}. Reset token: {} | Access URL: /reset-password?token={}",
                email, rawToken, rawToken);
    }

    @Override
    @Transactional
    public void resetPassword(PasswordResetRequest request) {
        if (request.getToken() == null || request.getToken().trim().isEmpty()) {
            throw new IllegalArgumentException("Reset token is required");
        }
        if (request.getNewPassword() == null || request.getNewPassword().length() < 8) {
            throw new IllegalArgumentException("New password must be at least 8 characters long");
        }

        String tokenHash = hashToken(request.getToken().trim());
        PasswordResetToken token = passwordResetTokenRepository
                .findByTokenHashAndUsedFalseAndExpiresAtAfter(tokenHash, LocalDateTime.now())
                .orElseThrow(() -> new IllegalArgumentException("Password reset token is invalid or has expired"));

        User user = token.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        token.setUsed(true);
        passwordResetTokenRepository.save(token);

        logger.info("Password successfully updated for user: {}", user.getEmail());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> getCurrentAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return userRepository.findByEmailIgnoreCase(auth.getName());
        }
        return Optional.empty();
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getCurrentUserDto() {
        return getCurrentAuthenticatedUser().map(this::toDto).orElse(null);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm missing", e);
        }
    }

    private UserDto toDto(User u) {
        return new UserDto(
                u.getId(),
                u.getEmail(),
                u.getFullName(),
                u.getPhone(),
                u.getRole(),
                u.isEnabled(),
                u.getCreatedAt()
        );
    }
}
