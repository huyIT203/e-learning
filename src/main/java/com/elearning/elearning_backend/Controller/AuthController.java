package com.elearning.elearning_backend.Controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.elearning.elearning_backend.DTO.AuthResponse;
import com.elearning.elearning_backend.DTO.ChangePasswordRequest;
import com.elearning.elearning_backend.DTO.LoginRequest;
import com.elearning.elearning_backend.DTO.RegisterRequest;
import com.elearning.elearning_backend.Model.User;
import com.elearning.elearning_backend.Repository.UserRepository;
import com.elearning.elearning_backend.Service.AuthService;
import com.elearning.elearning_backend.Service.ForgotPasswordService;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final ForgotPasswordService forgotPasswordService;
    private final UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request, HttpServletResponse response){
        AuthResponse authResponse = authService.register(request);
        
        // Set cookie for authentication
        ResponseCookie cookie = ResponseCookie.from("auth_token", authResponse.getToken())
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(60*60)
                .build();
                
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(authResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);

        ResponseCookie cookie = ResponseCookie.from("auth_token", response.getToken())
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(60*60)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE,cookie.toString())
                .body(response);
    }

//    @PutMapping("/reset-password")
//    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequest request) {
//        authService.resetPassword(request);
//        return ResponseEntity.ok("Mật khẩu đã được thay đổi thành công.");
//    }
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgot(@RequestParam String email) {
        forgotPasswordService.sendResetToken(email);
        return ResponseEntity.ok("Đã gửi mã xác nhận OTP qua email.");
    }

    @PostMapping("/confirm-reset")
    public ResponseEntity<String> reset(@RequestParam String token, @RequestParam String newPassword) {
        forgotPasswordService.resetPassword(token, newPassword);
        return ResponseEntity.ok("Đã đặt lại mật khẩu thành công.");
    }

    @PutMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> changePassword(@AuthenticationPrincipal UserDetails userDetails,
                                                 @RequestBody ChangePasswordRequest request) {
        authService.changePassword(userDetails.getUsername(), request);
        return ResponseEntity.ok("Đổi mật khẩu thành công.");
    }
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        // Xóa cookie
        ResponseCookie deleteCookie = ResponseCookie.from("auth_token", "")
                .path("/")
                .maxAge(0)
                .httpOnly(true)
                .build();

        // Xóa context xác thực người dùng
        SecurityContextHolder.clearContext();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
                .build();
    }

    @GetMapping("/check")
    public ResponseEntity<Map<String, Object>> checkAuth() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            
            if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
                User user = userRepository.findByEmail(auth.getName()).orElse(null);
                
                if (user != null) {
                    response.put("authenticated", true);
                    response.put("user", user);
                    response.put("role", user.getRole().name());
                    response.put("userId", user.getId());
                    return ResponseEntity.ok(response);
                }
            }
            
            response.put("authenticated", false);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("authenticated", false);
            response.put("error", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }
}
