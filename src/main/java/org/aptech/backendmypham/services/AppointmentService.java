package org.aptech.backendmypham.services;

import org.aptech.backendmypham.dto.AppointmentDto;
import org.aptech.backendmypham.dto.AppointmentResponseDto;
import org.aptech.backendmypham.models.Appointment;

import java.util.List;

public interface AppointmentService {
    List<AppointmentResponseDto> getALlAppointment();
    void createAppointment(AppointmentDto dto);
    AppointmentResponseDto findByIdAndIsActive(Long AiD);
    void updateAppointment(Long Aid,AppointmentDto appointmentDto);
    void deleteAppointment(Long AiD);

      public List<AppointmentResponseDto> getAppointmentsByUserId(Long userId);
    void cancelAppointment(Long appointmentId);
}
