package com.elearning.elearning_backend.Security;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.elearning.elearning_backend.Model.User;
import com.elearning.elearning_backend.Repository.UserRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        String userEmail = null;

        String requestURI = request.getRequestURI();
        System.out.println("🟡 ĐANG TRUY CẬP: " + requestURI);

        // Skip filter for public endpoints
        if (isPublicEndpoint(requestURI)) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = null;
        
        // First, try to get JWT from Authorization header
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7);
            System.out.println("🟢 JWT found in Authorization header");
        } else {
            // If not in header, try to get from cookies
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if (cookie.getName().equals("auth_token")) {
                        jwt = cookie.getValue();
                        System.out.println("🟢 JWT found in cookies");
                        break;
                    }
                }
            }
            if (jwt == null) {
                System.out.println("❌ No JWT token found in header or cookies");
            }
        }

        boolean isTokenValid = false;
        if (jwt != null && !jwt.isBlank()) {
            try {
                userEmail = jwtService.extractEmail(jwt);
                System.out.println("🟢 Extracted email from JWT: " + userEmail);
                isTokenValid = true;
            } catch (Exception e) {
                System.out.println("❌ Error extracting email from JWT: " + e.getMessage());
                userEmail = null;
            }
        }

        try {
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
                User user = userRepository.findByEmail(userEmail).orElse(null);

                if (user != null && jwtService.isTokenValid(jwt, userDetails)) {
                    String role = jwtService.extractClaim(jwt, claims -> claims.get("role", String.class));
                    System.out.println("🟢 JWT is valid for user: " + userEmail + " with role: " + role);

                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + role))
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    System.out.println("🟢 Authentication set successfully for user: " + userEmail);
                } else {
                    if (user == null) {
                        System.out.println("❌ User not found in database: " + userEmail);
                    } else {
                        System.out.println("❌ JWT token is invalid for user: " + userEmail);
                    }
                    // Token is invalid or expired, redirect to session expired page
                    if (!isPublicEndpoint(requestURI) && !requestURI.startsWith("/api/")) {
                        response.sendRedirect("/session-expired");
                        return;
                    }
                }
            } else if (userEmail == null) {
                System.out.println("❌ No email extracted from JWT");
            } else {
                System.out.println("🟡 User already authenticated: " + SecurityContextHolder.getContext().getAuthentication().getName());
            }
        } catch (Exception ex) {
            System.out.println("⚠️ Không thể xác thực JWT: " + ex.getMessage());
            ex.printStackTrace();
            
            // If token validation fails and it's not an API request, redirect to session expired
            if (!isPublicEndpoint(requestURI) && !requestURI.startsWith("/api/")) {
                response.sendRedirect("/session-expired");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isPublicEndpoint(String requestURI) {
        return requestURI.startsWith("/api/auth/") ||
               requestURI.startsWith("/login") ||
               requestURI.startsWith("/register") ||
               requestURI.startsWith("/forgot-password") ||
               requestURI.startsWith("/session-expired") ||
               requestURI.startsWith("/error") ||
               requestURI.startsWith("/css/") ||
               requestURI.startsWith("/js/") ||
               requestURI.startsWith("/images/") ||
               requestURI.equals("/favicon.ico");
    }
}
