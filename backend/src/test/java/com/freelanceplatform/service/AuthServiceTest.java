package com.freelanceplatform.service;

import com.freelanceplatform.dto.auth.RegisterRequest;
import com.freelanceplatform.entity.Role;
import com.freelanceplatform.entity.UserAccount;
import com.freelanceplatform.repository.ClientProfileRepository;
import com.freelanceplatform.repository.FreelancerProfileRepository;
import com.freelanceplatform.repository.RoleRepository;
import com.freelanceplatform.repository.UserAccountRepository;
import com.freelanceplatform.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock UserAccountRepository users;
    @Mock RoleRepository roles;
    @Mock FreelancerProfileRepository freelancerProfiles;
    @Mock ClientProfileRepository clientProfiles;
    @Mock PasswordEncoder passwordEncoder;
    @Mock AuthenticationManager authenticationManager;
    @Mock JwtService jwtService;
    @InjectMocks AuthService authService;

    @Test
    void registrationHashesPasswordAndCreatesRoleProfile() {
        when(users.existsByEmailIgnoreCase("sam@example.com")).thenReturn(false);
        when(roles.findByName("FREELANCER")).thenReturn(Optional.of(new Role(2L, "FREELANCER")));
        when(passwordEncoder.encode("long-password" )).thenReturn("bcrypt-hash");
        when(users.save(any(UserAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.createToken(any(UserAccount.class))).thenReturn("signed-token");
        when(jwtService.expiresInSeconds()).thenReturn(1800L);

        var result = authService.register(new RegisterRequest("Sam Example", " Sam@Example.com ", "long-password", RegisterRequest.AccountRole.FREELANCER));

        assertThat(result.accessToken()).isEqualTo("signed-token");
        assertThat(result.user().email()).isEqualTo("sam@example.com");
        assertThat(result.user().roles()).containsExactly("FREELANCER");
        verify(users).save(argThat(user -> user.getPasswordHash().equals("bcrypt-hash") && user.getRoles().size() == 1));
        verify(freelancerProfiles).save(argThat(profile -> profile.getUser() != null));
        verifyNoInteractions(clientProfiles);
    }

    @Test
    void registrationRejectsDuplicateEmail() {
        when(users.existsByEmailIgnoreCase("sam@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(new RegisterRequest("Sam", "sam@example.com", "long-password", RegisterRequest.AccountRole.CLIENT)))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("already exists");

        verify(users, never()).save(any());
    }
}
