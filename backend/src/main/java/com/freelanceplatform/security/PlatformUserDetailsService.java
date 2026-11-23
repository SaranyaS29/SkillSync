package com.freelanceplatform.security;

import com.freelanceplatform.repository.UserAccountRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PlatformUserDetailsService implements UserDetailsService {
    private final UserAccountRepository users;
    public PlatformUserDetailsService(UserAccountRepository users) { this.users = users; }

    @Override @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        var account = users.findByEmailIgnoreCase(email).orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));
        return User.withUsername(account.getEmail()).password(account.getPasswordHash())
                .authorities(account.getRoles().stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName())).toList())
                .disabled(account.getStatus() != com.freelanceplatform.entity.UserAccount.AccountStatus.ACTIVE)
                .build();
    }
}
