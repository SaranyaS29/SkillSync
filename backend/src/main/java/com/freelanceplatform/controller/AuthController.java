package com.freelanceplatform.controller;

import com.freelanceplatform.dto.auth.AuthResponse;
import com.freelanceplatform.dto.auth.ChangePasswordRequest;
import com.freelanceplatform.dto.auth.ForgotPasswordRequest;
import com.freelanceplatform.dto.auth.LoginRequest;
import com.freelanceplatform.dto.auth.RegisterRequest;
import com.freelanceplatform.dto.auth.ResetPasswordRequest;
import com.freelanceplatform.dto.auth.UserResponse;
import com.freelanceplatform.service.AuthService;
import com.freelanceplatform.service.PasswordResetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication")
public class AuthController {
    private final AuthService authService;
    private final PasswordResetService passwordResetService;
    public AuthController(AuthService authService, PasswordResetService passwordResetService) { this.authService = authService; this.passwordResetService=passwordResetService; }

    @PostMapping("/register") @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a client or freelancer account")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) { return authService.register(request); }

    @PostMapping("/login")
    @Operation(summary = "Log in and receive a JWT access token")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) { return authService.login(request); }

    @PostMapping("/forgot-password") @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(summary = "Send a password reset link when the account exists")
    public java.util.Map<String, String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.request(request.email());
        return java.util.Map.of("message", "If an account exists for this email, a reset link will be sent.");
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset a password with a one-time emailed token")
    public java.util.Map<String, String> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.reset(request.token(), request.newPassword());
        return java.util.Map.of("message", "Password updated. You can now log in.");
    }

    @GetMapping("/me")
    @Operation(summary = "Get the authenticated account")
    public UserResponse me(Authentication authentication) { return authService.currentUser(authentication.getName()); }

    @PutMapping("/change-password")
    @Operation(summary = "Change the authenticated account password")
    public java.util.Map<String, String> changePassword(@Valid @RequestBody ChangePasswordRequest request, Authentication authentication) {
        authService.changePassword(authentication.getName(), request);
        return java.util.Map.of("message", "Password changed successfully.");
    }
}
