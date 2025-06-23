package org.aptech.backendmypham.services;

import org.aptech.backendmypham.dto.AppointmentDto;
import org.aptech.backendmypham.dto.AppointmentHistoryDTO;
import org.aptech.backendmypham.dto.AppointmentResponseDto;
import org.aptech.backendmypham.dto.AppointmentStatsDTO;
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


      public List<AppointmentResponseDto> getAppointmentsByUserId(Long userId);
    void cancelAppointment(Long appointmentId);
    public Map<String, Object> getAppointmentsGroupedByShift(LocalDate date, Long userId);

    List<AppointmentHistoryDTO> getCustomerAppointmentHistory(Long customerId, int page, int size);
    List<AppointmentHistoryDTO> getAppointmentHistoryByPhone(String phoneNumber);
    AppointmentStatsDTO getCustomerAppointmentStats(Long customerId);

}
