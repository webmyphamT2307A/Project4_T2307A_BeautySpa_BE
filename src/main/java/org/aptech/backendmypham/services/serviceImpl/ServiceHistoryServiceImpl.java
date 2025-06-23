package org.aptech.backendmypham.services.serviceImpl;

import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.aptech.backendmypham.dto.GuestServiceHistoryCreateDTO;
import org.aptech.backendmypham.dto.ServiceHistoryDTO;
import org.aptech.backendmypham.exception.ResourceNotFoundException;
import org.aptech.backendmypham.models.*;
import org.aptech.backendmypham.repositories.*;
import org.aptech.backendmypham.services.ServiceHistoryService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServiceHistoryServiceImpl implements ServiceHistoryService {
    private  final ServiceHistoryRepository serviceHistoryRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;
    private final ServiceRepository serviceRepository;
    private final ReviewRepository reviewRepository;

    @Override
    public List<ServiceHistoryDTO> getAll() {
        return serviceHistoryRepository.findAll().stream()
                .map(this::mapToDTO)
                .sorted((a, b) -> b.getId().compareTo(a.getId()))
                .collect(Collectors.toList());
    }
    @Override
    public List<ServiceHistoryDTO> getHistoryBycustomerId(Integer customerId) {
        return serviceHistoryRepository.findBycustomerId(customerId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    @Override
    public ServiceHistoryDTO createGuestServiceHistory(GuestServiceHistoryCreateDTO createDTO) {
        // --- B1: Tìm hoặc tạo khách hàng vãng lai ---
        // Dùng orElseGet để nếu không tìm thấy thì sẽ thực hiện logic tạo mới
        Customer customer = customerRepository
                .findFirstByEmailOrPhone(createDTO.getGuestEmail(), createDTO.getGuestPhone())
                .orElseGet(() -> {
                    // Nếu không tồn tại, tạo một customer mới
                    Customer newGuest = new Customer();
                    newGuest.setFullName(createDTO.getGuestName());
                    newGuest.setEmail(createDTO.getGuestEmail());
                    newGuest.setPhone(createDTO.getGuestPhone());
                    newGuest.setIsGuest(true); // Đánh dấu đây là tài khoản khách vãng lai
                    newGuest.setPassword(null); // Không có mật khẩu
                    newGuest.setIsActive(true);
                    return customerRepository.save(newGuest);
                });

        // --- B2: Lấy các đối tượng liên quan khác ---
        User user = userRepository.findById(createDTO.getUserId().longValue())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhân viên với ID: " + createDTO.getUserId()));

        Appointment appointment = appointmentRepository.findById(Long.valueOf(createDTO.getAppointmentId()))
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lịch hẹn với ID: " + createDTO.getAppointmentId()));

        org.aptech.backendmypham.models.Service service = serviceRepository.findById(createDTO.getServiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy dịch vụ với ID: " + createDTO.getServiceId()));


        // --- B3: Tạo và lưu lịch sử dịch vụ ---
        Servicehistory newHistory = new Servicehistory();
        newHistory.setCustomer(customer); // Liên kết với khách hàng vừa tìm/tạo
        newHistory.setUser(user);
        newHistory.setAppointment(appointment);
        newHistory.setService(service);
        newHistory.setNotes(createDTO.getNotes());
        newHistory.setDateUsed(Instant.now()); // Hoặc lấy từ appointment
        newHistory.setCreatedAt(Instant.now());
        newHistory.setIsActive(true);

        Servicehistory savedHistory = serviceHistoryRepository.save(newHistory);

        return mapToDTO(savedHistory);
    }


    private ServiceHistoryDTO mapToDTO(Servicehistory serviceHistory) {
        ServiceHistoryDTO dto = new ServiceHistoryDTO();

        dto.setId(serviceHistory.getId());
        dto.setUserId(serviceHistory.getUser() != null ? serviceHistory.getUser().getId().intValue() : null);

        // ← THÊM DÒNG NÀY để map tên nhân viên
        dto.setUserName(serviceHistory.getUser() != null ? serviceHistory.getUser().getFullName() : null);

        dto.setCustomerId(serviceHistory.getCustomer() != null ? serviceHistory.getCustomer().getId() : null);
        dto.setAppointmentId(serviceHistory.getAppointment() != null ? serviceHistory.getAppointment().getId().intValue() : null);
        dto.setServiceId(serviceHistory.getService() != null ? serviceHistory.getService().getId() : null);
        dto.setServiceName(serviceHistory.getService() != null ? serviceHistory.getService().getName() : null);
        dto.setPrice(serviceHistory.getService() != null ? serviceHistory.getService().getPrice() : null);
        dto.setAppointmentDate(serviceHistory.getAppointment() != null ? serviceHistory.getAppointment().getAppointmentDate() : null);
        dto.setNotes(serviceHistory.getNotes());
        dto.setCustomerName(serviceHistory.getCustomer() != null ? serviceHistory.getCustomer().getFullName() : null);
        dto.setCreatedAt(serviceHistory.getCreatedAt());
        dto.setIsActive(serviceHistory.getIsActive());

        return dto;
    }
    @Override
    public List<ServiceHistoryDTO> lookupHistory(String email, String phone) {
        // Lấy tất cả customers match email hoặc phone
        List<Customer> customers = customerRepository.findAllByEmailOrPhone(email, phone);

        if (!customers.isEmpty()) {
            List<ServiceHistoryDTO> allHistories = new ArrayList<>();

            // Lấy lịch sử của tất cả customers
            for (Customer customer : customers) {
                List<Servicehistory> histories = serviceHistoryRepository.findByCustomer_Id(customer.getId());
                allHistories.addAll(histories.stream()
                        .map(this::mapToDTO)
                        .collect(Collectors.toList()));
            }

            return allHistories;
        }

        return Collections.emptyList();
    }
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
                    .map(app -> app.getPrice() != null ? app.getPrice() : BigDecimal.ZERO)
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

                    Map<String, Object> orderMap = new LinkedHashMap<>();
                    orderMap.put("id", appointment.getId());
                    orderMap.put("date", appointment.getAppointmentDate());
                    orderMap.put("dateDisplay", dateDisplay);
                    orderMap.put("service", serviceName);
                    orderMap.put("startTime", appointment.getAppointmentDate());
                    orderMap.put("endTime", appointment.getEndTime());
                    orderMap.put("timeDisplay", timeDisplay);
                    orderMap.put("rating", null);
                    orderMap.put("commission", appointment.getPrice());
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
