package com.example.whostolemyfood.payment.base;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class AuditorAwareImpl implements AuditorAware<UUID> {

    @Override
    public Optional<UUID> getCurrentAuditor() {

       //return Optional.of(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        if (authentication.getPrincipal() instanceof UserDetails userDetails) {

            return Optional.of(UUID.fromString(userDetails.getUsername()));
        }

        return Optional.empty();
    }
}
