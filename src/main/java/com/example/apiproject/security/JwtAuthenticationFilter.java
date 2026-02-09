package com.example.apiproject.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtUtils jwtUtils, CustomUserDetailsService userDetailsService) {
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        System.out.println("1. [Filter] Request received for: " + request.getServletPath()); // TRACER
        // 1. Extract Authorization header
        String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;

        // 2. Check if header exists and starts with "Bearer "
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            // 3. Extract token (remove "Bearer " prefix)
            jwt = authorizationHeader.substring(7);
            System.out.println("2. [Filter] Token found: " + jwt.substring(0, 10) + "..."); // TRACER

            // 4. Extract username from token
            try {
                username = jwtUtils.extractUsername(jwt);
            } catch (Exception e) {
                // Token is malformed or invalid
                logger.error("Error extracting username from token: " + e.getMessage());
            }
        }

        // 5. If we have a username and no authentication is set in the context
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // 6. Load user details from database
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // 7. Validate token
            if (jwtUtils.validateToken(jwt, userDetails)) {
                System.out.println("3. [Filter] Token Valid. Setting SecurityContext for: " + username); // TRACER
                // 8. Create authentication token
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities());

                // Set additional details
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 9. Set authentication in SecurityContext
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        }

        // 10. Continue the filter chain
        filterChain.doFilter(request, response);
        System.out.println("4. [Filter] Response sent back."); // TRACER
    }
}
