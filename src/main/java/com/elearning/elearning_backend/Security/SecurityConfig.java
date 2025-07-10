package com.elearning.elearning_backend.Security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtFilter jwtFilter;
    private final UserDetailsService userDetailsService;


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .requiresChannel(channel -> channel
                    .requestMatchers(r -> r.getHeader("X-Forwarded-Proto") != null)
                    .requiresSecure())
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/", "/login", "/register", "/forgot-password", "/error", "/courses", "/welcome").permitAll()
                        
                        // Static resources
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico", "/uploads/**").permitAll()
                        
                        // WebSocket endpoints
                        .requestMatchers("/ws/**").permitAll()
                        
                        // Chat API endpoints
                        .requestMatchers("/api/chat/**").authenticated()
                        
                        // Admin routes
                        .requestMatchers("/admin/**", "/api/admin/**").hasRole("ADMIN")

                        // Teacher routes
                        .requestMatchers("/teacher/**").hasRole("TEACHER")
                        .requestMatchers("/api/courses/**").hasAnyRole("ADMIN", "TEACHER")
                        .requestMatchers("/api/courses/*/exams").hasAnyRole("ADMIN", "TEACHER")
                        .requestMatchers("/api/exams/*/questions").hasAnyRole("ADMIN", "TEACHER")
                        .requestMatchers("/api/exams/*/submissions").hasAnyRole("TEACHER", "ADMIN")
                        
                        // Quiz endpoints - accessible to all authenticated users
                        .requestMatchers("/api/quiz/**").authenticated()
                        .requestMatchers("/api/embedded-quiz/**").authenticated()
                        
                        // Dashboard and student routes
                        .requestMatchers("/dashboard").authenticated()
                        .requestMatchers("/student/**").hasRole("STUDENT")
                        .requestMatchers("/api/exams/*/submit").hasRole("STUDENT")
                        .requestMatchers("/api/exams/*/submission").hasRole("STUDENT")
                        
                        .anyRequest().authenticated()
                )
                // JWT Token Based Authentication
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .authenticationProvider(authenticationProvider())
                .build();
    }
    
    @Bean
    public AuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

}
