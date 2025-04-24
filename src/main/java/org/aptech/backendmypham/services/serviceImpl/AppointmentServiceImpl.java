package org.aptech.backendmypham.services.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.aptech.backendmypham.dto.AppointmentDto;
import org.aptech.backendmypham.dto.AppointmentResponseDto;
import org.aptech.backendmypham.models.Appointment;
import org.aptech.backendmypham.repositories.*;
import org.aptech.backendmypham.services.AppointmentService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final ServiceRepository serviceRepository;
    private final CustomerRepository customerRepository;
    private final BranchRepository branchRepository;
    private final TimeSlotsRepository timeSlotsRepository;

    @Override
    public void createAppointment(AppointmentDto dto) { Appointment appointment = new Appointment();

        appointment.setUser(userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy User")));
        appointment.setService(serviceRepository.findById(Math.toIntExact(dto.getServiceId()))
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Service")));
        appointment.setCustomer(customerRepository.findById(dto.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Customer")));
        appointment.setBranch(branchRepository.findById(dto.getBranchId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Branch")));
        appointment.setTimeSlot(timeSlotsRepository.findById(dto.getTimeSlotId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy TimeSlot")));

        // Convert ngày
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate date = LocalDate.parse(dto.getAppointmentDate(), formatter);
        Instant startOfDay = date.atStartOfDay(ZoneId.systemDefault()).toInstant();
        appointment.setAppointmentDate(startOfDay);

        // Tùy bạn muốn tính `endTime` như thế nào (ví dụ +1 giờ)
        appointment.setEndTime(startOfDay.plusSeconds(3600)); // cộng 1 tiếng

        appointment.setSlot(dto.getSlot());
        appointment.setStatus(dto.getStatus());
        appointment.setNotes(dto.getNotes());
        appointment.setPhoneNumber(dto.getPhoneNumber());
        appointment.setFullName(dto.getFullName());
        appointment.setPrice(BigDecimal.valueOf(dto.getPrice()));

        appointment.setCreatedAt(Instant.now());
        appointment.setUpdatedAt(Instant.now());
        appointment.setIsActive(true);

        appointmentRepository.save(appointment);
    }
    @Override
    public AppointmentResponseDto findByIdAndIsActive(Long id) {
        Appointment appointment = appointmentRepository.findByIdAndIsActive(id, true)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch hẹn"));

        return convertToDto(appointment);
    }

    private AppointmentResponseDto convertToDto(Appointment appointment) {
        AppointmentResponseDto dto = new AppointmentResponseDto();
        dto.setId(appointment.getId());
        dto.setFullName(appointment.getFullName());
        dto.setPhoneNumber(appointment.getPhoneNumber());
        dto.setStatus(appointment.getStatus());
        dto.setSlot(appointment.getSlot());
        dto.setNotes(appointment.getNotes());
        dto.setAppointmentDate(appointment.getAppointmentDate().toString());
        dto.setEndTime(appointment.getEndTime().toString());
        dto.setPrice(appointment.getPrice());

        dto.setServiceName(appointment.getService().getName());
        dto.setBranchName(appointment.getBranch().getName());
        dto.setCustomerName(appointment.getCustomer().getFullName());

        return dto;
    }

    @Override
    public void updateAppointment(Long Aid, AppointmentDto dto) {
        Appointment appointment = appointmentRepository.findByIdAndIsActive(Aid, true)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch hẹn"));

        appointment.setFullName(dto.getFullName());
        appointment.setPhoneNumber(dto.getPhoneNumber());
        appointment.setNotes(dto.getNotes());
        appointment.setStatus(dto.getStatus());

        // Nếu muốn cập nhật ngày
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate date = LocalDate.parse(dto.getAppointmentDate(), formatter);
        Instant startOfDay = date.atStartOfDay(ZoneId.systemDefault()).toInstant();
        appointment.setAppointmentDate(startOfDay);
        appointment.setEndTime(startOfDay.plusSeconds(3600));

        appointment.setUpdatedAt(Instant.now());

        appointmentRepository.save(appointment);
    }
    @Override
    public void deleteAppointment(Long Aid) {
        try {
            Appointment appointment = appointmentRepository.findByIdAndIsActive(Aid, true)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch hẹn"));

            appointment.setIsActive(false);
            appointment.setUpdatedAt(Instant.now());
            appointmentRepository.save(appointment);
        } catch (RuntimeException e) {
            throw new RuntimeException("Xóa mềm thất bại: " + e.getMessage());
        }
    }



}
