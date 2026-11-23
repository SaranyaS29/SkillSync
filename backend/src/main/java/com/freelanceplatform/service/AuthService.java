package com.freelanceplatform.service;

import com.freelanceplatform.dto.auth.AuthResponse;
import com.freelanceplatform.dto.auth.ChangePasswordRequest;
import com.freelanceplatform.dto.auth.LoginRequest;
import com.freelanceplatform.dto.auth.RegisterRequest;
import com.freelanceplatform.dto.auth.UserResponse;
import com.freelanceplatform.entity.ClientProfile;
import com.freelanceplatform.entity.FreelancerProfile;
import com.freelanceplatform.entity.Role;
import com.freelanceplatform.entity.UserAccount;
import com.freelanceplatform.repository.ClientProfileRepository;
import com.freelanceplatform.repository.FreelancerProfileRepository;
import com.freelanceplatform.repository.RoleRepository;
import com.freelanceplatform.repository.UserAccountRepository;
import com.freelanceplatform.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthService {
    private final UserAccountRepository users;
    private final RoleRepository roles;
    private final FreelancerProfileRepository freelancerProfiles;
    private final ClientProfileRepository clientProfiles;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(UserAccountRepository users, RoleRepository roles,
                       FreelancerProfileRepository freelancerProfiles, ClientProfileRepository clientProfiles,
                       PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JwtService jwtService) {
        this.users = users; this.roles = roles; this.freelancerProfiles = freelancerProfiles;
        this.clientProfiles = clientProfiles; this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager; this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (users.existsByEmailIgnoreCase(email)) throw new IllegalArgumentException("An account with this email already exists");
        String roleName = request.role().name();
        Role role = roles.findByName(roleName).orElseThrow(() -> new IllegalStateException("Configured role is missing: " + roleName));
        UserAccount user = new UserAccount();
        user.setEmail(email);
        user.setFullName(request.fullName().trim());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.getRoles().add(role);
        user = users.save(user);
        if (request.role() == RegisterRequest.AccountRole.FREELANCER) {
            FreelancerProfile profile = new FreelancerProfile(); profile.setUser(user); freelancerProfiles.save(profile);
        } else {
            ClientProfile profile = new ClientProfile(); profile.setUser(user); clientProfiles.save(profile);
        }
        return response(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, request.password()));
        UserAccount user = users.findByEmailIgnoreCase(email).orElseThrow();
        return response(user);
    }

    @Transactional(readOnly = true)
    public UserResponse currentUser(String email) {
        UserAccount user = users.findByEmailIgnoreCase(email).orElseThrow(() -> new IllegalArgumentException("Account not found"));
        return new UserResponse(user.getId(), user.getFullName(), user.getEmail(), user.getStatus().name(), roleNames(user));
    }

    @Transactional
    public void changePassword(String email, ChangePasswordRequest request) {
        UserAccount user = users.findByEmailIgnoreCase(email).orElseThrow(() -> new IllegalArgumentException("Account not found"));
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash()))
            throw new IllegalArgumentException("Current password is incorrect");
        if (passwordEncoder.matches(request.newPassword(), user.getPasswordHash()))
            throw new IllegalArgumentException("New password must be different from the current password");
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
    }

    private AuthResponse response(UserAccount user) {
        return new AuthResponse(jwtService.createToken(user), "Bearer", jwtService.expiresInSeconds(),
                new AuthResponse.UserSummary(user.getId(), user.getFullName(), user.getEmail(), roleNames(user)));
    }

    private Set<String> roleNames(UserAccount user) {
        return user.getRoles().stream().map(Role::getName).collect(Collectors.toUnmodifiableSet());
    }
}
