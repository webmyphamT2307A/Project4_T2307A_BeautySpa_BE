package org.aptech.backendmypham.services;

import org.aptech.backendmypham.dto.AppointmentDto;
import org.aptech.backendmypham.dto.AppointmentResponseDto;
import org.springframework.data.relational.core.sql.In;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface AppointmentService {
    List<AppointmentResponseDto> getALlAppointment();
    void createAppointment(AppointmentDto dto);
    AppointmentResponseDto findByIdAndIsActive(Long AiD);
    void updateAppointment(Long Aid,AppointmentDto appointmentDto);
    void deleteAppointment(Long AiD);


    void cancelAppointment(Long appointmentId);

    Map<String, Object> getAppointmentsGroupedByShift(LocalDate date, Long userId);
    List<AppointmentResponseDto> getAppointmentsByUserId(Long userId);
    void markServiceAsComplete(Long serviceId);
}
