package org.aptech.backendmypham.services.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.aptech.backendmypham.dto.ServiceRequestDto;
import org.aptech.backendmypham.dto.ServiceResponseDto;
import org.aptech.backendmypham.exception.ResourceNotFoundException;
import org.aptech.backendmypham.models.Appointment;
import org.aptech.backendmypham.models.Service;
import org.aptech.backendmypham.repositories.AppointmentRepository;
import org.aptech.backendmypham.repositories.ReviewRepository;
import org.aptech.backendmypham.repositories.ServiceRepository;
import org.aptech.backendmypham.services.ServicesService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
@RequiredArgsConstructor

public class ServicesServiceImpl implements ServicesService {

    final private ServiceRepository serviceRepository;
    private final ReviewRepository reviewRepository;
    private final AppointmentRepository appointmentRepository;
    @Override
    public List<ServiceResponseDto> getAllService() {
        return serviceRepository.findAll().stream()
                .map(this::toServiceResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ServiceResponseDto> findById(Integer id) {
        return serviceRepository.findById(id).map(this::toServiceResponse);
    }

    @Override
    public Optional<ServiceResponseDto> findByName(String name) {
        return serviceRepository.findByName(name).map(this::toServiceResponse);
    }

    @Override
    public ServiceResponseDto createService(ServiceRequestDto serviceRequest) {
        Service newService = this.toServiceEntity(serviceRequest);
        newService.setCreatedAt(Instant.now());
        newService.setIsActive(true);
        Service savedService = serviceRepository.save(newService);
        return this.toServiceResponse(savedService);
    }

    @Override
    public ServiceResponseDto updateService(Integer id, ServiceRequestDto serviceRequest) {
        Service existingService = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy dịch vụ với ID = " + id));

        existingService.setName(serviceRequest.getName());
        existingService.setDescription(serviceRequest.getDescription());
        existingService.setPrice(serviceRequest.getPrice());
        existingService.setImageUrl(serviceRequest.getImageUrl());
        existingService.setDuration(serviceRequest.getDuration());

        Service updatedService = serviceRepository.save(existingService);

        return this.toServiceResponse(updatedService);
    }

    @Override
    public void softDeleteService(Integer id) {
        Service service = serviceRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service không tồn tại hoặc đã bị vô hiệu hóa với ID = " + id));

        service.setIsActive(false);
        serviceRepository.save(service);
    }


    private ServiceResponseDto toServiceResponse(Service service) {
        if (service == null) return null;
        ServiceResponseDto dto = new ServiceResponseDto();
        dto.setId(service.getId());
        dto.setName(service.getName());
        dto.setDescription(service.getDescription());
        dto.setPrice(service.getPrice());
        dto.setDuration(service.getDuration());
        dto.setImageUrl(service.getImageUrl());
        dto.setCreatedAt(service.getCreatedAt());
        dto.setIsActive(service.getIsActive());
        return dto;
    }

    private Service toServiceEntity(ServiceRequestDto request) {
        if (request == null) return null;
        Service entity = new Service();
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        entity.setPrice(request.getPrice());
        entity.setDuration(request.getDuration());
        entity.setImageUrl(request.getImageUrl());
        entity.setCreatedAt(Instant.now());
        entity.setIsActive(true);
        return entity;
    }
    @Override
    public Map<String, List<Object>> getMonthlyHistory(Long userId, Integer year, Integer month) {
        // Lấy danh sách lịch hẹn theo userId, year và month
        List<Appointment> appointments = appointmentRepository.findAppointmentsByUserIdAndDate(userId, year, month);

        // Nhóm theo MM/yyyy
        Map<String, List<Appointment>> groupedAppointments = appointments.stream()
                .collect(Collectors.groupingBy(appointment -> appointment.getAppointmentDate()
                        .atZone(ZoneId.systemDefault())
                        .format(DateTimeFormatter.ofPattern("MM/yyyy"))));

        Map<String, List<Object>> result = new LinkedHashMap<>();

        for (Map.Entry<String, List<Appointment>> entry : groupedAppointments.entrySet()) {
            String monthYear = entry.getKey();
            List<Appointment> monthlyAppointments = entry.getValue();

            // Tính toán tổng quan
            int workDays = (int) monthlyAppointments.stream()
                    .map(app -> app.getAppointmentDate().atZone(ZoneId.systemDefault()).toLocalDate())
                    .distinct()
                    .count();

            int totalAppointments = monthlyAppointments.size();
            BigDecimal baseSalary = calculateBaseSalary(workDays);
            BigDecimal totalCommission = monthlyAppointments.stream()
                    .filter(app -> "completed".equals(app.getStatus()))
                    .map(app -> {
                        BigDecimal price = app.getPrice() != null ? app.getPrice() : BigDecimal.ZERO;
                        return price.multiply(BigDecimal.valueOf(0.1)); // 10%
                    })
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalEarnings = baseSalary.add(totalCommission);

            List<Object> monthlyData = new ArrayList<>();

            // Thêm dòng tổng kết
            monthlyData.add(Map.of(
                    "isSummary", true,
                    "month", monthYear,
                    "workDays", workDays,
                    "totalOrders", totalAppointments,
                    "baseSalary", baseSalary,
                    "totalCommission", totalCommission,
                    "totalEarnings", totalEarnings
            ));

            // Thêm chi tiết từng lịch hẹn
            for (Appointment appointment : monthlyAppointments) {
                try {
                    //get rating from reviews if exists
//                    Integer rating = reviewRepository.findByAppointmentAndIsActiveTrue(appointment)
//                            .map(Review::getRating)
//                            .orElse(null);

                    String serviceName = appointment.getService() != null
                            ? appointment.getService().getName()
                            : "Không rõ";

                    String dateDisplay = appointment.getAppointmentDate().atZone(ZoneId.systemDefault())
                            .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

                    String timeDisplay = appointment.getAppointmentDate().atZone(ZoneId.systemDefault())
                            .format(DateTimeFormatter.ofPattern("HH:mm")) + " - " +
                            appointment.getEndTime().atZone(ZoneId.systemDefault())
                                    .format(DateTimeFormatter.ofPattern("HH:mm"));
                    BigDecimal commission = appointment.getPrice() != null
                            ? appointment.getPrice()
                            .multiply(BigDecimal.valueOf(0.1))
                            .setScale(2, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO;

                    Map<String, Object> orderMap = new LinkedHashMap<>();
                    orderMap.put("id", appointment.getId());
                    orderMap.put("date", appointment.getAppointmentDate());
                    orderMap.put("dateDisplay", dateDisplay);
                    orderMap.put("service", serviceName);
                    orderMap.put("startTime", appointment.getAppointmentDate());
                    orderMap.put("endTime", appointment.getEndTime());
                    orderMap.put("timeDisplay", timeDisplay);
                    orderMap.put("rating", null);
                    orderMap.put("commission", commission);
                    orderMap.put("status", appointment.getStatus());

                    monthlyData.add(orderMap);
                } catch (Exception ex) {
                    System.err.println("Lỗi khi xử lý appointment ID: " + appointment.getId());
                    ex.printStackTrace();
                }
            }

            result.put(monthYear, monthlyData);
        }

        System.out.println("Monthly History Result: " + result);
        return result;
    }

    private BigDecimal calculateBaseSalary(int workDays) {
        // Example calculation for base salary
        return BigDecimal.valueOf(workDays * 400000); // 400,000 per workday
    }
}