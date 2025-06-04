package org.aptech.backendmypham.services.serviceImpl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.aptech.backendmypham.dto.AppointmentDto;
import org.aptech.backendmypham.dto.AppointmentResponseDto;
import org.aptech.backendmypham.models.*;
import org.aptech.backendmypham.repositories.*;
import org.aptech.backendmypham.services.AppointmentService;
import org.aptech.backendmypham.services.BookingService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final ServiceRepository serviceRepository;
    private final CustomerRepository customerRepository;
    private  final BookingService bookingService;
    private final BookingRepository bookingRepository;
    private final BranchRepository branchRepository;
    private final TimeSlotsRepository timeSlotsRepository;
    private  final ServiceHistoryRepository serviceHistoryRepository;

    @Override
    @Transactional
    public void createAppointment(AppointmentDto dto) {
        Appointment appointment = new Appointment();

        // 1. Xử lý User
        if (dto.getUserId() != null) {
            User user = userRepository.findById(dto.getUserId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy User với ID: " + dto.getUserId()));
            appointment.setUser(user);
        } else {
            appointment.setUser(null); // Cho phép không có nhân viên cụ thể cho Appointment
        }

        // 2. Xử lý Service
        // Giả sử Service ID trong DB là Integer, nếu là Long thì dto.getServiceId() nên là Long
        org.aptech.backendmypham.models.Service service = serviceRepository.findById(Math.toIntExact(dto.getServiceId()))
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Service với ID: " + dto.getServiceId()));
        appointment.setService(service);

        // 3. Xử lý Customer
        if (dto.getCustomerId() != null) {
            Customer customer = customerRepository.findById(dto.getCustomerId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy Customer với ID: " + dto.getCustomerId()));
            appointment.setCustomer(customer);
        } else {
            // Nếu không có customerId, bạn có thể muốn tạo một Customer mới dựa trên
            // dto.getFullName() và dto.getPhoneNumber() ở đây nếu nghiệp vụ yêu cầu.
            // Hoặc cho phép Customer là null cho Appointment.
            appointment.setCustomer(null);
        }

        // 4. Xử lý Branch và TimeSlot
        Branch branch = branchRepository.findById(dto.getBranchId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Branch với ID: " + dto.getBranchId()));
        appointment.setBranch(branch);

        Timeslots timeSlot = timeSlotsRepository.findById(dto.getTimeSlotId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy TimeSlot với ID: " + dto.getTimeSlotId()));
        appointment.setTimeSlot(timeSlot);

        // 5. Xử lý Thời gian cho Appointment và Booking
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate parsedDate = LocalDate.parse(dto.getAppointmentDate(), formatter);
        LocalTime slotStartTime = timeSlot.getStartTime(); // Giả sử TimeSlot có getStartTime() trả về LocalTime

        if (slotStartTime == null) {
            throw new RuntimeException("TimeSlot không có thời gian bắt đầu (startTime).");
        }

        LocalDateTime localBookingStartDateTime = parsedDate.atTime(slotStartTime);
        // Thay vì ZoneId.systemDefault() nếu bạn không chắc chắn múi giờ server
        Instant bookingStartInstant = localBookingStartDateTime.atZone(ZoneId.of("Asia/Ho_Chi_Minh")).toInstant();

        appointment.setAppointmentDate(bookingStartInstant); // Thời gian bắt đầu thực sự của lịch hẹn

        Integer durationMinutes = dto.getDurationMinutes();
        if (durationMinutes == null || durationMinutes <= 0) {
            // Lấy duration từ service nếu DTO không cung cấp hoặc không hợp lệ
            // Giả sử Service entity có trường durationMinutes
            // durationMinutes = service.getDurationMinutes(); // Cần thêm trường này vào Service entity
            // Hoặc mặc định
            durationMinutes = 60; // Mặc định 60 phút nếu không có
        }
        appointment.setEndTime(bookingStartInstant.plus(durationMinutes, ChronoUnit.MINUTES));


        // 6. KIỂM TRA NHÂN VIÊN RẢNH (NGAY TRƯỚC KHI LƯU)
        if (appointment.getUser() != null) { // Chỉ kiểm tra nếu có nhân viên được chỉ định
            boolean staffIsActuallyAvailable = bookingService.isStaffAvailable(
                    appointment.getUser().getId(),
                    bookingStartInstant,
                    durationMinutes
            );
            if (!staffIsActuallyAvailable) {
                throw new RuntimeException("Nhân viên " + appointment.getUser().getFullName() + " đã có lịch vào thời điểm này. Vui lòng chọn thời gian hoặc nhân viên khác.");
            }
        }
        // (Tùy chọn: Kiểm tra số slot còn trống cho TimeSlot + Date nếu cần)


        // 7. Các thông tin khác cho Appointment
        appointment.setSlot(dto.getSlot());
        appointment.setStatus(dto.getStatus() != null ? dto.getStatus() : "pending");
        appointment.setNotes(dto.getNotes());
        appointment.setPhoneNumber(dto.getPhoneNumber());
        appointment.setFullName(dto.getFullName());
        appointment.setPrice(BigDecimal.valueOf(dto.getPrice() != null ? dto.getPrice() : 0.0));
        appointment.setCreatedAt(Instant.now());
        appointment.setUpdatedAt(Instant.now());
        appointment.setIsActive(true);

        // 8. Lưu Appointment
        Appointment savedAppointment = appointmentRepository.save(appointment);

        // 9. TẠO VÀ LƯU BOOKING (nếu có đủ thông tin bắt buộc cho Booking)
        // Giả định: Booking yêu cầu User và Customer phải có.
        if (savedAppointment.getUser() != null && savedAppointment.getCustomer() != null) {
            Booking booking = new Booking();
            booking.setUser(savedAppointment.getUser());
            booking.setCustomer(savedAppointment.getCustomer());
            booking.setService(savedAppointment.getService());
            booking.setBookingDateTime(bookingStartInstant); // Thời gian bắt đầu
            booking.setStatus(savedAppointment.getStatus());
            booking.setNotes(savedAppointment.getNotes());
            booking.setTotalPrice(BigDecimal.valueOf(savedAppointment.getPrice().doubleValue()));
            booking.setDurationMinutes(durationMinutes); // Thời lượng
            booking.setCreatedAt(Instant.now());
            booking.setUpdatedAt(Instant.now());
            booking.setIsActive(true); // Hoặc từ DTO nếu AppointmentDto có trường này cho Booking
            bookingRepository.save(booking);
        } else {
            System.out.println("Thông báo: Không tạo Booking do thiếu User hoặc Customer trong Appointment.");
            // Hoặc bạn có thể ném lỗi nếu việc không tạo được Booking là không chấp nhận được
            // throw new RuntimeException("Không thể tạo Booking do thiếu thông tin User hoặc Customer.");
        }

        // 10. TẠO VÀ LƯU SERVICE HISTORY
        Servicehistory serviceHistory = new Servicehistory();
        serviceHistory.setUser(savedAppointment.getUser());
        serviceHistory.setCustomer(savedAppointment.getCustomer());
        serviceHistory.setAppointment(savedAppointment);
        serviceHistory.setService(savedAppointment.getService());
        serviceHistory.setDateUsed(Instant.now());
        serviceHistory.setNotes("Lịch sử lưu tự động khi tạo lịch hẹn.");
        serviceHistory.setCreatedAt(Instant.now());
        serviceHistory.setIsActive(true);
        serviceHistoryRepository.save(serviceHistory);

        // Không cần gọi appointmentRepository.save(appointment) lần nữa ở cuối
        // vì `savedAppointment` đã là đối tượng được quản lý sau lần save đầu tiên.
        // Nếu bạn có thay đổi gì trên `savedAppointment` sau đó thì JPA sẽ tự động flush khi giao dịch kết thúc.
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
