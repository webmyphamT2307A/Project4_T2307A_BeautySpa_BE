package org.aptech.backendmypham.services.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.aptech.backendmypham.dto.AppointmentDto;
import org.aptech.backendmypham.dto.AppointmentResponseDto;
import org.aptech.backendmypham.models.Appointment;
import org.aptech.backendmypham.models.Servicehistory;
import org.aptech.backendmypham.repositories.*;
import org.aptech.backendmypham.services.AppointmentService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final ServiceRepository serviceRepository;
    private final CustomerRepository customerRepository;
    private final BranchRepository branchRepository;
    private final TimeSlotsRepository timeSlotsRepository;
    private  final ServiceHistoryRepository serviceHistoryRepository;

    @Override
    public void createAppointment(AppointmentDto dto) {
        Appointment appointment = new Appointment();

        // Nếu có userId thì set, không thì để null
        if (dto.getUserId() != null) {
            appointment.setUser(userRepository.findById(dto.getUserId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy User")));
        } else {
            appointment.setUser(null);
        }

        appointment.setService(serviceRepository.findById(Math.toIntExact(dto.getServiceId()))
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Service")));

        // Nếu có customerId thì set, không thì để null
        if (dto.getCustomerId() != null) {
            appointment.setCustomer(customerRepository.findById(dto.getCustomerId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy Customer")));
        } else {
            appointment.setCustomer(null);
        }

        appointment.setBranch(branchRepository.findById(dto.getBranchId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Branch")));
        appointment.setTimeSlot(timeSlotsRepository.findById(dto.getTimeSlotId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy TimeSlot")));

        // Convert ngày
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate date = LocalDate.parse(dto.getAppointmentDate(), formatter);
        Instant startOfDay = date.atStartOfDay(ZoneId.systemDefault()).toInstant();
        appointment.setAppointmentDate(startOfDay);

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
        // Lưu thông tin lịch hẹn vào cơ sở dữ liệu
        Appointment savedAppointment = appointmentRepository.save(appointment);

        // Tạo service history (lịch sử dịch vụ)
        Servicehistory serviceHistory = new Servicehistory();
        serviceHistory.setUser(savedAppointment.getUser()); // User thực hiện dịch vụ
        serviceHistory.setCustomer(savedAppointment.getCustomer()); // Khách hàng
        serviceHistory.setAppointment(savedAppointment); // Lịch hẹn
        serviceHistory.setService(savedAppointment.getService()); // Dịch vụ sử dụng
        serviceHistory.setDateUsed(Instant.now()); // Ngày sử dụng dịch vụ (thời điểm hiện tại)
        serviceHistory.setNotes("Lịch sử lưu tự động khi tạo lịch"); // Có thể nhận từ DTO nếu cần
        serviceHistory.setCreatedAt(Instant.now());
        serviceHistory.setIsActive(true);

        // Lưu service history vào cơ sở dữ liệu
        serviceHistoryRepository.save(serviceHistory);


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
        dto.setCustomerImageUrl(appointment.getCustomer() != null ? appointment.getCustomer().getImageUrl() : null);
        dto.setPrice(appointment.getPrice());
        dto.setUserImageUrl(appointment.getUser() != null ? appointment.getUser().getImageUrl() : null);

        dto.setServiceName(appointment.getService().getName());
        dto.setBranchName(appointment.getBranch().getName());
        dto.setCustomerName(appointment.getCustomer().getFullName());

        if (appointment.getCustomer() != null) {
            dto.setCustomerName(appointment.getCustomer().getFullName());
            dto.setCustomerImageUrl(appointment.getCustomer().getImageUrl());
        } else {
            dto.setCustomerName("N/A");
            dto.setCustomerImageUrl(null);
        }
        if (appointment.getUser() != null) {
            dto.setUserName(appointment.getUser().getFullName());
            dto.setUserImageUrl(appointment.getUser().getImageUrl());
        } else {
            dto.setUserName("N/A");
            dto.setUserImageUrl(null);
        }

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
    @Override
    public List<AppointmentResponseDto> getAppointmentsByUserId(Long userId) {
        List<Appointment> appointments = appointmentRepository.findAllByUserIdAndIsActive(userId);

        return appointments.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    @Override
    public List<AppointmentResponseDto> getALlAppointment() {
        List<Appointment> appointments = appointmentRepository.findAll();
        appointments.sort((a1, a2) -> a2.getId().compareTo(a1.getId()));

        return appointments.stream()
                .map(appointment -> {
                    try {
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

                        if (appointment.getUser() != null) {
                            dto.setUserName(appointment.getUser().getFullName());
                        } else {
                            dto.setUserName("N/A");
                        }

                        // Set service name if available
                        if (appointment.getService() != null) {
                            dto.setServiceName(appointment.getService().getName());
                        } else {
                            dto.setServiceName("N/A");
                        }

                        // Set branch name if available
                        if (appointment.getBranch() != null) {
                            dto.setBranchName(appointment.getBranch().getName());
                        } else {
                            dto.setBranchName("N/A");
                        }

                        // Set customer name & image if available
                        if (appointment.getCustomer() != null) {
                            dto.setCustomerName(appointment.getCustomer().getFullName());
                            dto.setCustomerImageUrl(appointment.getCustomer().getImageUrl()); // <-- BỔ SUNG DÒNG NÀY
                        } else {
                            dto.setCustomerName("N/A");
                            dto.setCustomerImageUrl(null);
                        }
                        if(appointment.getUser() != null) {
                            dto.setUserName(appointment.getUser().getFullName());
                            dto.setUserImageUrl(appointment.getUser().getImageUrl());
                        }else{
                            dto.setUserName("N/A");
                            dto.setUserImageUrl(null);
                        }

                        return dto;
                    } catch (Exception e) {
                        // Create a basic DTO with available information
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

                        dto.setUserName("N/A");
                        dto.setServiceName("N/A");
                        dto.setBranchName("N/A");
                        dto.setCustomerName("N/A");
                        dto.setCustomerImageUrl(null);

                        return dto;
                    }
                })
                .collect(Collectors.toList());
    }


}
