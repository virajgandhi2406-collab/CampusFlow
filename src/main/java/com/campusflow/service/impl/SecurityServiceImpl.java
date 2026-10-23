package com.campusflow.service.impl;

import com.campusflow.entity.User;
import com.campusflow.exception.AccessDeniedException;
import com.campusflow.repository.UserRepository;
import com.campusflow.service.SecurityService;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class SecurityServiceImpl implements SecurityService {

    private final UserRepository userRepository;

    public SecurityServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean isAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken);
    }

    @Override
    public String getCurrentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return null;
        }
        return auth.getName();
    }

    @Override
    public User getCurrentUser() {
        String email = getCurrentUserEmail();
        if (email == null) {
            throw new AccessDeniedException("User is not authenticated");
        }
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new AccessDeniedException("Authenticated user not found in database"));
    }

    @Override
    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }
}
