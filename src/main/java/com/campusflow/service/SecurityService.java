package com.campusflow.service;

import com.campusflow.entity.User;

public interface SecurityService {
    User getCurrentUser();
    Long getCurrentUserId();
    String getCurrentUserEmail();
    boolean isAuthenticated();
}
