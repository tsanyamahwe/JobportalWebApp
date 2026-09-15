package com.learn2code.jobportal.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        assert userDetails != null;
        String username = userDetails.getUsername();
        System.out.println("The username : " + username + " is logged in");
        boolean hasJobSeekerRole = authentication.getAuthorities().stream().anyMatch(r -> Objects.equals(r.getAuthority(), "Job Seeker"));
        boolean hasRecruiterRole = authentication.getAuthorities().stream().anyMatch(r -> Objects.equals(r.getAuthority(), "Recruiter"));
        if (hasJobSeekerRole || hasRecruiterRole) {
            response.sendRedirect("/dashboard");
        }
    }
}
