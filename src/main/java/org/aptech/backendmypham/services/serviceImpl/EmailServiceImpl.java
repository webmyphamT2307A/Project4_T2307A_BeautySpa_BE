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
        // ... (Nội dung HTML giữ nguyên)
        // Chỉ thay đổi cách gọi formatDate
        String notesSection = "";
        if (request.getNotes() != null && !request.getNotes().trim().isEmpty()) {
            notesSection = String.format(
                    "<div style='background-color: #fff3cd; border: 1px solid #ffeaa7; padding: 15px; border-radius: 5px; margin: 20px 0;'>" +
                            "<strong style='color: #856404;'>📝 Ghi chú đặc biệt:</strong><br>" +
                            "<span style='color: #856404; font-style: italic;'>%s</span>" +
                            "</div>", request.getNotes());
        }

        return String.format("""
            <!DOCTYPE html>
            <html>
            ...
                                <tr style="border-bottom: 1px solid #e9ecef;">
                                    <td style="padding: 8px 0; font-weight: 600; color: #495057;">Ngày hẹn:</td>
                                    <td style="padding: 8px 0; color: #2c3e50; text-align: right;">%s</td>
                                </tr>
            ...
            </html>
            """,
                companyName,
                request.getCustomerName(),
                request.getAppointmentId(),
                request.getServiceName(),
                formatDate(request.getAppointmentDate()), // <--- Thay đổi ở đây
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