package org.aptech.backendmypham.services.serviceImpl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aptech.backendmypham.dto.EmailConfirmationRequestDto;
import org.aptech.backendmypham.services.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;


@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    // private final CustomerRepository customerRepository; // ĐÃ XÓA vì không dùng đến

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.company.name:Beauty Spa}")
    private String companyName;

    @Value("${app.company.address:123 Beauty Street, Spa City}")
    private String companyAddress;

    @Value("${app.company.phone:+84 123 456 789}")
    private String companyPhone;

    @Override // Thêm @Override cho rõ ràng
    public boolean sendAppointmentConfirmation(EmailConfirmationRequestDto request) {
        try {
            String emailContent = createEmailHtml(request);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, companyName);
            helper.setTo(request.getCustomerEmail());
            helper.setSubject("Xác Nhận Lịch Hẹn - " + companyName + " #" + request.getAppointmentId());
            helper.setText(emailContent, true); // true để kích hoạt HTML

            mailSender.send(message);

            log.info("Appointment confirmation email sent successfully to: {}", request.getCustomerEmail());

        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("Failed to send appointment confirmation email to: {}", request.getCustomerEmail(), e);
            // Throw exception để controller có thể bắt và xử lý
            throw new RuntimeException("Error occurred while sending email: " + e.getMessage());
        }
        return false;
    }

    private String createEmailHtml(EmailConfirmationRequestDto request) {
        String notesSection = "";
        if (request.getNotes() != null && !request.getNotes().trim().isEmpty()) {
            notesSection = String.format(
                    "<div style='background-color: #fff3cd; border: 1px solid #ffeaa7; padding: 15px; border-radius: 5px; margin: 20px 0;'>" +
                            "<strong style='color: #856404;'>📝 Special Notes:</strong><br>" +
                            "<span style='color: #856404; font-style: italic;'>%s</span>" +
                            "</div>", request.getNotes());
        }

        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Appointment Confirmation</title>
            </head>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; background-color: #f4f4f4;">
                <div style="background-color: #ffffff; margin: 20px auto; padding: 0; border-radius: 10px; box-shadow: 0 0 20px rgba(0,0,0,0.1); overflow: hidden;">
                    
                    <!-- Header -->
                    <div style="background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: white; padding: 30px 40px; text-align: center;">
                        <h1 style="margin: 0; font-size: 28px; font-weight: 300;">%s</h1>
                        <p style="margin: 10px 0 0 0; font-size: 16px;">Appointment Confirmation</p>
                    </div>

                    <!-- Content -->
                    <div style="padding: 40px;">
                        <div style="font-size: 18px; margin-bottom: 30px; color: #2c3e50;">
                            <p>Dear <strong>%s</strong>,</p>
                            <p>Thank you for booking an appointment with us! We're excited to serve you and help you look and feel your best.</p>
                        </div>

                        <!-- Appointment Details -->
                        <div style="background-color: #f8f9fa; border-left: 4px solid #667eea; padding: 25px; margin: 30px 0; border-radius: 5px;">
                            <h2 style="margin-top: 0; color: #667eea;">📅 Appointment Details</h2>
                            
                            <table style="width: 100%%; border-collapse: collapse;">
                                <tr style="border-bottom: 1px solid #e9ecef;">
                                    <td style="padding: 8px 0; font-weight: 600; color: #495057; width: 40%%;">Appointment ID:</td>
                                    <td style="padding: 8px 0; color: #2c3e50; text-align: right;">#%s</td>
                                </tr>
                                <tr style="border-bottom: 1px solid #e9ecef;">
                                    <td style="padding: 8px 0; font-weight: 600; color: #495057;">Service:</td>
                                    <td style="padding: 8px 0; color: #2c3e50; text-align: right;">%s</td>
                                </tr>
                                <tr style="border-bottom: 1px solid #e9ecef;">
                                    <td style="padding: 8px 0; font-weight: 600; color: #495057;">Date:</td>
                                    <td style="padding: 8px 0; color: #2c3e50; text-align: right;">%s</td>
                                </tr>
                                <tr style="border-bottom: 1px solid #e9ecef;">
                                    <td style="padding: 8px 0; font-weight: 600; color: #495057;">Time:</td>
                                    <td style="padding: 8px 0; color: #2c3e50; text-align: right;">%s - %s</td>
                                </tr>
                                <tr style="border-bottom: 1px solid #e9ecef;">
                                    <td style="padding: 8px 0; font-weight: 600; color: #495057;">Staff Member:</td>
                                    <td style="padding: 8px 0; color: #2c3e50; text-align: right;">%s</td>
                                </tr>
                                <tr style="border-bottom: 1px solid #e9ecef;">
                                    <td style="padding: 8px 0; font-weight: 600; color: #495057;">Location:</td>
                                    <td style="padding: 8px 0; color: #2c3e50; text-align: right;">%s</td>
                                </tr>
                                <tr>
                                    <td style="padding: 8px 0; font-weight: 600; color: #495057;">Total Price:</td>
                                    <td style="padding: 8px 0; color: #28a745; font-weight: bold; font-size: 18px; text-align: right;">$%.2f</td>
                                </tr>
                            </table>
                        </div>

                        %s

                        <!-- Important Information -->
                        <div style="background-color: #d1ecf1; border: 1px solid #bee5eb; padding: 20px; border-radius: 5px; margin: 25px 0;">
                            <h3 style="color: #0c5460; margin-top: 0;">📋 Important Information</h3>
                            <ul style="color: #0c5460; margin: 10px 0; padding-left: 20px;">
                                <li><strong>Arrival:</strong> Please arrive 10-15 minutes early for check-in</li>
                                <li><strong>Cancellation:</strong> Please call us at least 24 hours in advance to reschedule</li>
                                <li><strong>Payment:</strong> We accept cash, credit cards, and digital payments</li>
                                <li><strong>What to bring:</strong> Just yourself! We provide all necessary items</li>
                            </ul>
                        </div>

                        <p style="text-align: center; color: #6c757d; margin-top: 30px;">
                            If you have any questions or need to make changes to your appointment, 
                            please don't hesitate to contact us.
                        </p>
                    </div>

                    <!-- Footer -->
                    <div style="background-color: #2c3e50; color: #ecf0f1; padding: 30px 40px; text-align: center;">
                        <div style="margin: 15px 0;">
                            <h3 style="margin: 0 0 10px 0;">%s</h3>
                            <p style="margin: 5px 0; font-size: 14px;">%s</p>
                            <p style="margin: 5px 0; font-size: 14px;">📞 %s</p>
                            <p style="margin: 5px 0; font-size: 14px;">📧 %s</p>
                        </div>
                        
                        <p style="font-size: 12px; color: #95a5a6; margin-top: 20px;">
                            © %d %s. All rights reserved.
                        </p>
                    </div>
                </div>
            </body>
            </html>
            """,
                companyName,
                request.getCustomerName(),
                request.getAppointmentId(),
                request.getServiceName(),
                formatDate(request.getAppointmentDate()),
                request.getAppointmentTime(),
                request.getEndTime() != null ? request.getEndTime() : "N/A",
                request.getStaffName() != null ? request.getStaffName() : "Sẽ được chỉ định",
                request.getBranchName(),
                request.getPrice(),
                notesSection,
                companyName,
                companyAddress,
                companyPhone,
                fromEmail,
                LocalDateTime.now().getYear(),
                companyName
        );
    }

    // Hàm formatDate được cải tiến, an toàn hơn
    private String formatDate(String dateString) {
        if (dateString == null || dateString.isBlank()) {
            return "N/A";
        }
        try {
            // Cắt chuỗi tại 'T' nếu có (để xử lý định dạng ISO "2024-12-31T17:00:00.000Z")
            String datePart = dateString.split("T")[0];
            LocalDate date = LocalDate.parse(datePart, DateTimeFormatter.ISO_LOCAL_DATE);
            return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (DateTimeParseException e) {
            log.warn("Could not parse date string: '{}'. Returning original value.", dateString, e);
            return dateString; // Trả về giá trị gốc nếu không parse được
        }
    }
}