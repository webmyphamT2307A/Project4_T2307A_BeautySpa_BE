package org.aptech.backendmypham.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GuestAppointmentRequestDto  {
    private String fullName;
    private String phoneNumber;

    // Thông tin lịch sử dịch vụ
    @NotNull(message = "User ID (nhân viên thực hiện) không được để trống")
    private Integer userId;

    @NotNull(message = "Appointment ID không được để trống")
    private Integer appointmentId;
    private String appointmentDate;
    private String email;

    @NotNull(message = "Service ID không được để trống")
    private Integer serviceId;
    private Long timeSlotId;

    private String notes;
}
