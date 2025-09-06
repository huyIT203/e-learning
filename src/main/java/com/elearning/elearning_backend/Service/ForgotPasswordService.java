package com.elearning.elearning_backend.Service;

import java.util.Date;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.elearning.elearning_backend.Model.PasswordResetToken;
import com.elearning.elearning_backend.Repository.PasswordResetTokenRepository;
import com.elearning.elearning_backend.Repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ForgotPasswordService {
    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    public void sendResetToken(String email) {
        if (!userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email không tồn tại");
        }

        String token = UUID.randomUUID().toString();
        Date expiry = new Date(System.currentTimeMillis() + 15 * 60 * 1000);

        tokenRepository.deleteByEmail(email);
        tokenRepository.save(new PasswordResetToken(null, email, token, expiry));

        // 🎨 Beautiful HTML Email Template for Password Reset
        String subject = "🔐 Khôi phục mật khẩu E-learning";
        String title = "Khôi phục mật khẩu tài khoản";
        String content = buildPasswordResetEmailContent(token, email);

        emailService.sendDirectHtmlEmail(email, subject, content);
    }

    private String buildPasswordResetEmailContent(String token, String email) {
        StringBuilder content = new StringBuilder();
        
        // Complete HTML email template with modern design
        content.append("<!DOCTYPE html>");
        content.append("<html lang='vi'>");
        content.append("<head>");
        content.append("<meta charset='UTF-8'>");
        content.append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        content.append("<title>Khôi phục mật khẩu E-learning</title>");
        content.append("<style>");
        content.append("@import url('https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;500;600;700&display=swap');");
        content.append("</style>");
        content.append("</head>");
        content.append("<body style='margin: 0; padding: 0; font-family: \"Poppins\", Arial, Helvetica, sans-serif; background-color: #f8f9fc; line-height: 1.6;'>");
        
        // Main container with shadow and rounded corners
        content.append("<div style='max-width: 600px; margin: 40px auto; background-color: #ffffff; border-radius: 20px; box-shadow: 0 10px 30px rgba(0,0,0,0.1); overflow: hidden;'>");
        
        // Header with gradient background
        content.append("<div style='background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); padding: 50px 30px; text-align: center; position: relative;'>");
        content.append("<div style='position: absolute; top: 0; left: 0; right: 0; bottom: 0; background: url(\"data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%23ffffff' fill-opacity='0.05'%3E%3Ccircle cx='30' cy='30' r='4'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E\") repeat; opacity: 0.3;'></div>");
        content.append("<div style='position: relative; z-index: 1;'>");
        content.append("<div style='background: rgba(255,255,255,0.15); width: 80px; height: 80px; border-radius: 50%; display: flex; align-items: center; justify-content: center; margin: 0 auto 20px auto; backdrop-filter: blur(10px);'>");
        content.append("<div style='font-size: 36px;'>🔐</div>");
        content.append("</div>");
        content.append("<h1 style='color: #ffffff; margin: 0 0 10px 0; font-size: 28px; font-weight: 600; letter-spacing: -0.5px;'>");
        content.append("🎓 E-Learning System");
        content.append("</h1>");
        content.append("<p style='color: rgba(255,255,255,0.9); margin: 0; font-size: 16px; font-weight: 300;'>");
        content.append("Hệ thống học tập trực tuyến");
        content.append("</p>");
        content.append("</div>");
        content.append("</div>");
        
        // Content body with beautiful spacing
        content.append("<div style='padding: 50px 40px;'>");
        
        // Welcome message
        content.append("<div style='text-align: center; margin-bottom: 40px;'>");
        content.append("<h2 style='color: #2d3748; margin: 0 0 15px 0; font-size: 24px; font-weight: 600;'>");
        content.append("🔑 Khôi phục mật khẩu");
        content.append("</h2>");
        content.append("<p style='color: #718096; margin: 0; font-size: 16px; line-height: 1.5;'>");
        content.append("Chúng tôi đã nhận được yêu cầu khôi phục mật khẩu cho tài khoản của bạn");
        content.append("</p>");
        content.append("</div>");
        
        // User info section
        content.append("<div style='background: linear-gradient(135deg, #f7fafc 0%, #edf2f7 100%); padding: 25px; border-radius: 16px; margin-bottom: 35px; border: 1px solid #e2e8f0;'>");
        content.append("<div style='display: flex; align-items: center; gap: 15px;'>");
        content.append("<div style='background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); width: 50px; height: 50px; border-radius: 12px; display: flex; align-items: center; justify-content: center; color: white; font-size: 20px; font-weight: 600;'>");
        content.append("👤");
        content.append("</div>");
        content.append("<div>");
        content.append("<div style='font-size: 14px; color: #718096; margin-bottom: 2px;'>Tài khoản email</div>");
        content.append("<div style='font-size: 16px; font-weight: 600; color: #2d3748;'>").append(email).append("</div>");
        content.append("</div>");
        content.append("</div>");
        content.append("</div>");
        
        // OTP Code section - main highlight
        content.append("<div style='background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); padding: 35px; border-radius: 20px; margin-bottom: 35px; text-align: center; position: relative; overflow: hidden;'>");
        content.append("<div style='position: absolute; top: -50px; right: -50px; width: 100px; height: 100px; background: rgba(255,255,255,0.1); border-radius: 50%;'></div>");
        content.append("<div style='position: absolute; bottom: -30px; left: -30px; width: 60px; height: 60px; background: rgba(255,255,255,0.1); border-radius: 50%;'></div>");
        content.append("<div style='position: relative; z-index: 1;'>");
        content.append("<h3 style='color: #ffffff; margin: 0 0 20px 0; font-size: 18px; font-weight: 500;'>");
        content.append("🔢 Mã xác nhận của bạn");
        content.append("</h3>");
        content.append("<div style='background: rgba(255,255,255,0.95); padding: 20px 30px; border-radius: 12px; display: inline-block; margin-bottom: 20px;'>");
        content.append("<div style='font-size: 32px; font-weight: 700; color: #667eea; letter-spacing: 8px; font-family: \"Courier New\", monospace;'>");
        content.append(token);
        content.append("</div>");
        content.append("</div>");
        content.append("<div style='color: rgba(255,255,255,0.9); font-size: 14px; margin-top: 15px;'>");
        content.append("⏰ Mã có hiệu lực trong <strong>15 phút</strong>");
        content.append("</div>");
        content.append("</div>");
        content.append("</div>");
        
        // Instructions section
        content.append("<div style='background: #f7fafc; padding: 30px; border-radius: 16px; margin-bottom: 35px; border-left: 4px solid #667eea;'>");
        content.append("<h4 style='color: #2d3748; margin: 0 0 15px 0; font-size: 16px; font-weight: 600;'>");
        content.append("📋 Hướng dẫn sử dụng");
        content.append("</h4>");
        content.append("<ul style='color: #4a5568; margin: 0; padding-left: 20px; font-size: 14px; line-height: 1.8;'>");
        content.append("<li style='margin-bottom: 8px;'>Copy mã xác nhận ở trên</li>");
        content.append("<li style='margin-bottom: 8px;'>Quay lại trang khôi phục mật khẩu</li>");
        content.append("<li style='margin-bottom: 8px;'>Dán mã vào ô \"Mã xác nhận\"</li>");
        content.append("<li style='margin-bottom: 8px;'>Nhập mật khẩu mới và xác nhận</li>");
        content.append("<li>Nhấn \"Đặt lại mật khẩu\" để hoàn tất</li>");
        content.append("</ul>");
        content.append("</div>");
        
        // Action button
        content.append("<div style='text-align: center; margin-bottom: 35px;'>");
        content.append("<a href='http://localhost:8081/forgot-password' style='background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; text-decoration: none; padding: 18px 40px; border-radius: 50px; font-size: 16px; font-weight: 600; display: inline-block; box-shadow: 0 8px 25px rgba(102, 126, 234, 0.3); transition: all 0.3s ease; border: none;'>");
        content.append("🔓 Tiếp tục khôi phục mật khẩu");
        content.append("</a>");
        content.append("</div>");
        
        // Security notice
        content.append("<div style='background: linear-gradient(135deg, #fed7d7 0%, #feb2b2 100%); padding: 25px; border-radius: 16px; margin-bottom: 30px; border: 1px solid #fc8181;'>");
        content.append("<div style='display: flex; align-items: flex-start; gap: 15px;'>");
        content.append("<div style='background: #e53e3e; color: white; width: 40px; height: 40px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 18px; flex-shrink: 0;'>");
        content.append("⚠️");
        content.append("</div>");
        content.append("<div>");
        content.append("<h4 style='color: #742a2a; margin: 0 0 10px 0; font-size: 16px; font-weight: 600;'>");
        content.append("🛡️ Lưu ý bảo mật");
        content.append("</h4>");
        content.append("<ul style='color: #742a2a; margin: 0; padding-left: 20px; font-size: 14px; line-height: 1.6;'>");
        content.append("<li style='margin-bottom: 5px;'>Nếu bạn không yêu cầu khôi phục mật khẩu, hãy bỏ qua email này</li>");
        content.append("<li style='margin-bottom: 5px;'>Không chia sẻ mã xác nhận với bất kỳ ai</li>");
        content.append("<li>Mã sẽ tự động hết hạn sau 15 phút</li>");
        content.append("</ul>");
        content.append("</div>");
        content.append("</div>");
        content.append("</div>");
        
        // Timestamp
        content.append("<div style='text-align: center; padding: 20px; background: #f7fafc; border-radius: 12px; margin-bottom: 20px;'>");
        content.append("<p style='margin: 0; color: #718096; font-size: 14px;'>");
        content.append("🕒 Thời gian gửi: <strong>").append(new Date().toString()).append("</strong>");
        content.append("</p>");
        content.append("</div>");
        
        content.append("</div>"); // End content body
        
        // Footer with gradient
        content.append("<div style='background: linear-gradient(135deg, #2d3748 0%, #1a202c 100%); padding: 40px 30px; text-align: center;'>");
        content.append("<div style='margin-bottom: 25px;'>");
        content.append("<h3 style='color: #ffffff; margin: 0 0 10px 0; font-size: 20px; font-weight: 600;'>");
        content.append("🎓 E-Learning System");
        content.append("</h3>");
        content.append("<p style='color: #a0aec0; margin: 0; font-size: 14px;'>");
        content.append("Hệ thống học tập trực tuyến hiện đại và chuyên nghiệp");
        content.append("</p>");
        content.append("</div>");
        
        // Social links (optional)
        content.append("<div style='margin-bottom: 25px;'>");
        content.append("<div style='display: inline-flex; gap: 15px;'>");
        content.append("<div style='background: rgba(255,255,255,0.1); width: 40px; height: 40px; border-radius: 50%; display: flex; align-items: center; justify-content: center; color: #a0aec0; font-size: 16px;'>📧</div>");
        content.append("<div style='background: rgba(255,255,255,0.1); width: 40px; height: 40px; border-radius: 50%; display: flex; align-items: center; justify-content: center; color: #a0aec0; font-size: 16px;'>🌐</div>");
        content.append("<div style='background: rgba(255,255,255,0.1); width: 40px; height: 40px; border-radius: 50%; display: flex; align-items: center; justify-content: center; color: #a0aec0; font-size: 16px;'>📱</div>");
        content.append("</div>");
        content.append("</div>");
        
        // Copyright
        content.append("<div style='border-top: 1px solid #4a5568; padding-top: 25px;'>");
        content.append("<p style='color: #718096; margin: 0; font-size: 12px; line-height: 1.5;'>");
        content.append("© 2024 E-Learning System. Tất cả quyền được bảo lưu.<br>");
        content.append("Email này được gửi tự động từ hệ thống. Vui lòng không trả lời email này.<br>");
        content.append("Địa chỉ: Trường Đại học Công nghệ Thông tin, TP.HCM");
        content.append("</p>");
        content.append("</div>");
        content.append("</div>");
        
        content.append("</div>"); // End main container
        content.append("</body>");
        content.append("</html>");
        
        return content.toString();
    }

    public void resetPassword(String token, String newPassword) {
        var resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token không hợp lệ"));

        if (resetToken.getExpiryDate().before(new Date())) {
            throw new RuntimeException("Token đã hết hạn");
        }

        var user = userRepository.findByEmail(resetToken.getEmail())
                .orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        tokenRepository.deleteByEmail(resetToken.getEmail());

    }

}
