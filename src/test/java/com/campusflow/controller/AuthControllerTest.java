package com.campusflow.controller;

import com.campusflow.service.SecurityService;
import com.campusflow.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private SecurityService securityService;

    @Test
    @DisplayName("Should display login page for unauthenticated visitors")
    void testShowLoginForm() throws Exception {
        when(securityService.isAuthenticated()).thenReturn(false);

        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"));
    }

    @Test
    @DisplayName("Should display registration form for unauthenticated visitors")
    void testShowRegistrationForm() throws Exception {
        when(securityService.isAuthenticated()).thenReturn(false);

        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"))
                .andExpect(model().attributeExists("registrationDto"));
    }

    @Test
    @DisplayName("Should fail registration with validation errors when inputs are missing")
    void testProcessRegistration_ValidationFailure() throws Exception {
        when(securityService.isAuthenticated()).thenReturn(false);

        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("fullName", "")
                        .param("email", "invalid-email")
                        .param("password", "short")
                        .param("confirmPassword", "different"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"))
                .andExpect(model().hasErrors());
    }

    @Test
    @DisplayName("Should redirect unauthenticated requests from /dashboard to /login")
    void testProtectedDashboardRedirect() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }
}
