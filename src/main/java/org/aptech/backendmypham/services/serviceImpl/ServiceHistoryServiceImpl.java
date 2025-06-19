package org.aptech.backendmypham.services.serviceImpl;

import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.aptech.backendmypham.dto.GuestServiceHistoryCreateDTO;
import org.aptech.backendmypham.dto.ServiceHistoryDTO;
import org.aptech.backendmypham.exception.ResourceNotFoundException;
import org.aptech.backendmypham.models.Appointment;
import org.aptech.backendmypham.models.Customer;
import org.aptech.backendmypham.models.Servicehistory;
import org.aptech.backendmypham.models.User;
import org.aptech.backendmypham.repositories.*;
import org.aptech.backendmypham.services.ServiceHistoryService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServiceHistoryServiceImpl implements ServiceHistoryService {
    private  final ServiceHistoryRepository serviceHistoryRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;
    private final ServiceRepository serviceRepository;


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
                .findByEmailOrPhone(createDTO.getGuestEmail(), createDTO.getGuestPhone())
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
    @Override
    public List<ServiceHistoryDTO> lookupHistory(String email, String phone) {
        // Tìm khách hàng (kể cả khách vãng lai) bằng email hoặc sđt
        Optional<Customer> customerOpt = customerRepository.findByEmailOrPhone(email, phone);

        if (customerOpt.isPresent()) {
            Customer customer = customerOpt.get();
            // Nếu tìm thấy, lấy lịch sử theo customerId
            return serviceHistoryRepository.findByCustomer_Id(customer.getId()).stream()
                    .map(this::mapToDTO)
                    .collect(Collectors.toList());
        } else {
            // Nếu không tìm thấy khách hàng, trả về danh sách rỗng
            return Collections.emptyList();
        }
    }

    private ServiceHistoryDTO mapToDTO(Servicehistory history) {
        return new ServiceHistoryDTO(
                history.getId(),
                Math.toIntExact(history.getUser().getId()),
                history.getCustomer().getId(),
                history.getAppointment().getId(),
                history.getService().getId(),
                history.getService().getName(),
                history.getService().getPrice(),
                history.getAppointment().getAppointmentDate(),
                history.getNotes(),
                history.getCreatedAt(),
                history.getIsActive()
        );
    }
}
