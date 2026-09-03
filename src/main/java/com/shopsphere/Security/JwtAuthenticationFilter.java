package com.shopsphere.Security;

import com.shopsphere.Service.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter
        extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(
            JwtService jwtService) {

        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader =
                request.getHeader("Authorization");

        System.out.println(
                "Authorization Header: " + authHeader
        );

        if (authHeader == null ||
                !authHeader.startsWith("Bearer ")) {

            filterChain.doFilter(request, response);
            return;
        }

        String token =
                authHeader.substring(7);

        try {

            if (jwtService.isTokenValid(token)) {

                System.out.println("JWT VALID");

                String email =
                        jwtService.extractEmail(token);

                String role =
                        jwtService.extractRole(token);

                System.out.println(
                        "Logged in user: " + email
                );

                System.out.println(
                        "User role: " + role
                );

                String authority =
                        "ROLE_" + role;

                System.out.println(
                        "Authority: " + authority
                );

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                email,
                                null,
                                List.of(
                                        new SimpleGrantedAuthority(
                                                authority
                                        )
                                )
                        );

                SecurityContextHolder
                        .getContext()
                        .setAuthentication(authentication);

                System.out.println(
                        "Authentication set: "
                                + SecurityContextHolder
                                .getContext()
                                .getAuthentication()
                );

            } else {

                System.out.println(
                        "JWT INVALID"
                );
            }

        } catch (Exception e) {

            System.out.println(
                    "JWT ERROR: " + e.getMessage()
            );

            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}