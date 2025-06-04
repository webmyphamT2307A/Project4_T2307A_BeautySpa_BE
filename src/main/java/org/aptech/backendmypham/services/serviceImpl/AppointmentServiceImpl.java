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
import java.time.format.DateTimeParseException;
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
    @Transactional
    public void updateAppointment(Long appointmentId, AppointmentDto dto) {
        System.out.println("---- Bắt đầu updateAppointment cho ID: " + appointmentId + " ----");
        System.out.println("DTO nhận được: " + dto.toString()); // Giả sử AppointmentDto có toString() hợp lý

        Appointment appointment = appointmentRepository.findByIdAndIsActive(appointmentId, true)
                .orElseThrow(() -> {
                    System.err.println("LỖI: Không tìm thấy lịch hẹn (ID: " + appointmentId + ") hoặc lịch hẹn không active.");
                    return new RuntimeException("Không tìm thấy lịch hẹn (ID: " + appointmentId + ") hoặc lịch hẹn không active.");
                });

        System.out.println("Appointment tìm thấy: ID=" + appointment.getId() + ", Status cũ=" + appointment.getStatus());

        String oldStatus = appointment.getStatus();
        String newStatus = dto.getStatus();
        System.out.println("Status cũ: " + oldStatus + ", Status mới từ DTO: " + newStatus);

        // 1. Cập nhật các thông tin không phải thời gian từ DTO
        if (dto.getFullName() != null) {
            System.out.println("Updating fullName: " + dto.getFullName());
            appointment.setFullName(dto.getFullName());
        }
        if (dto.getPhoneNumber() != null) {
            System.out.println("Updating phoneNumber: " + dto.getPhoneNumber());
            appointment.setPhoneNumber(dto.getPhoneNumber());
        }
        if (dto.getNotes() != null) {
            System.out.println("Updating notes: " + dto.getNotes());
            appointment.setNotes(dto.getNotes());
        }
        if (newStatus != null) {
            System.out.println("Updating status to: " + newStatus);
            appointment.setStatus(newStatus);
        }
        // Thêm log cho các trường khác nếu cần thiết (branchId, customerId,...)

        // 2. Xử lý cập nhật NGÀY và GIỜ
        boolean timeChanged = false;
        Instant newBookingStartInstant = appointment.getAppointmentDate(); // Giữ giờ cũ làm mặc định
        Integer newDurationMinutes = (appointment.getService() != null && appointment.getService().getDuration() != null)
                ? appointment.getService().getDuration()
                : 60; // Lấy duration mặc định hoặc từ service hiện tại trước

        System.out.println("Thời gian Appointment hiện tại (UTC): " + newBookingStartInstant);

        try {
            if (dto.getAppointmentDate() != null && !dto.getAppointmentDate().isEmpty() && dto.getTimeSlotId() != null) {
                System.out.println("Phát hiện yêu cầu thay đổi cả Ngày và TimeSlot.");
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                LocalDate parsedNewDate = LocalDate.parse(dto.getAppointmentDate(), formatter);
                System.out.println("Parsed new date: " + parsedNewDate);

                Timeslots newTimeSlot = timeSlotsRepository.findById(dto.getTimeSlotId())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy TimeSlot mới với ID: " + dto.getTimeSlotId()));
                System.out.println("Found new TimeSlot ID: " + newTimeSlot.getSlotId() + " với startTime: " + newTimeSlot.getStartTime());

                LocalTime newSlotStartTime = newTimeSlot.getStartTime();
                if (newSlotStartTime == null) {
                    throw new RuntimeException("TimeSlot mới không có thời gian bắt đầu (startTime).");
                }

                LocalDateTime newLocalBookingStartDateTime = parsedNewDate.atTime(newSlotStartTime);
                newBookingStartInstant = newLocalBookingStartDateTime.atZone(ZoneId.of("Asia/Ho_Chi_Minh")).toInstant();
                timeChanged = true;
                appointment.setTimeSlot(newTimeSlot);
                System.out.println("Thời gian Appointment MỚI (thay đổi cả Ngày và Slot) (UTC): " + newBookingStartInstant);

            } else if (dto.getAppointmentDate() != null && !dto.getAppointmentDate().isEmpty()) {
                System.out.println("Phát hiện yêu cầu chỉ thay đổi Ngày.");
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                LocalDate parsedNewDate = LocalDate.parse(dto.getAppointmentDate(), formatter);
                System.out.println("Parsed new date: " + parsedNewDate);

                LocalTime currentSlotStartTime = appointment.getTimeSlot().getStartTime();
                System.out.println("Giữ nguyên startTime từ TimeSlot cũ: " + currentSlotStartTime);

                LocalDateTime newLocalBookingStartDateTime = parsedNewDate.atTime(currentSlotStartTime);
                newBookingStartInstant = newLocalBookingStartDateTime.atZone(ZoneId.of("Asia/Ho_Chi_Minh")).toInstant();
                timeChanged = true;
                System.out.println("Thời gian Appointment MỚI (chỉ thay đổi Ngày) (UTC): " + newBookingStartInstant);

            } else if (dto.getTimeSlotId() != null && (appointment.getTimeSlot() == null || !dto.getTimeSlotId().equals(appointment.getTimeSlot().getSlotId()))) {
                System.out.println("Phát hiện yêu cầu chỉ thay đổi TimeSlot.");
                Timeslots newTimeSlot = timeSlotsRepository.findById(dto.getTimeSlotId())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy TimeSlot mới với ID: " + dto.getTimeSlotId()));
                System.out.println("Found new TimeSlot ID: " + newTimeSlot.getSlotId() + " với startTime: " + newTimeSlot.getStartTime());

                LocalTime newSlotStartTime = newTimeSlot.getStartTime();
                LocalDate currentAppointmentDatePart = LocalDateTime.ofInstant(appointment.getAppointmentDate(), ZoneId.of("Asia/Ho_Chi_Minh")).toLocalDate();
                System.out.println("Giữ nguyên DatePart từ Appointment cũ: " + currentAppointmentDatePart);

                LocalDateTime newLocalBookingStartDateTime = currentAppointmentDatePart.atTime(newSlotStartTime);
                newBookingStartInstant = newLocalBookingStartDateTime.atZone(ZoneId.of("Asia/Ho_Chi_Minh")).toInstant();
                timeChanged = true;
                appointment.setTimeSlot(newTimeSlot);
                System.out.println("Thời gian Appointment MỚI (chỉ thay đổi Slot) (UTC): " + newBookingStartInstant);
            }
        } catch (DateTimeParseException e) {
            System.err.println("LỖI PARSE DATE: " + e.getMessage());
            throw new RuntimeException("Định dạng ngày tháng không hợp lệ. Vui lòng sử dụng dd/MM/yyyy.", e);
        } catch (RuntimeException e) {
            System.err.println("LỖI RUNTIME khi xử lý ngày/giờ: " + e.getMessage());
            throw e; // Ném lại lỗi để controller có thể bắt và trả về 400 hoặc 500
        }

        // Cập nhật Service nếu có thay đổi
        boolean serviceChanged = false;
        if (dto.getServiceId() != null && (appointment.getService() == null || !dto.getServiceId().equals(appointment.getService().getId().longValue()))) {
            System.out.println("Phát hiện yêu cầu thay đổi Service. Old Service ID: " + (appointment.getService() != null ? appointment.getService().getId() : "null") + ", New Service ID: " + dto.getServiceId());
            org.aptech.backendmypham.models.Service newService = serviceRepository.findById(Math.toIntExact(dto.getServiceId()))
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy Service với ID: " + dto.getServiceId()));
            appointment.setService(newService);
            serviceChanged = true;
            System.out.println("Đã cập nhật Service sang ID: " + newService.getId());
        }

        // Xác định durationMinutes (cần được tính toán lại nếu service hoặc DTO có duration)
        if (serviceChanged && appointment.getService() != null && appointment.getService().getDuration() != null) {
            newDurationMinutes = appointment.getService().getDuration();
            System.out.println("Duration được cập nhật từ Service mới: " + newDurationMinutes + " phút");
        } else if (dto.getDurationMinutes() != null && dto.getDurationMinutes() > 0) {
            newDurationMinutes = dto.getDurationMinutes();
            System.out.println("Duration được cập nhật từ DTO: " + newDurationMinutes + " phút");
        } else if (appointment.getService() != null && appointment.getService().getDuration() != null) {
            // Nếu service không đổi và DTO không có duration, dùng duration của service hiện tại
            newDurationMinutes = appointment.getService().getDuration();
        } else {
            // Giữ nguyên duration cũ nếu không có thông tin mới, hoặc đặt mặc định
            // Để lấy duration cũ, ta cần tính từ endTime và startTime cũ của Appointment
            // Hoặc Booking có lưu duration. Giả sử Booking lưu duration.
            Booking oldBooking = null;
            if(appointment.getUser() != null && appointment.getService() != null){
                List<Booking> bookings = bookingRepository.findByUserIdAndServiceIdAndBookingDateTimeAndIsActiveTrue(appointment.getUser().getId(), appointment.getService().getId(), appointment.getAppointmentDate());
                if(!bookings.isEmpty()) oldBooking = bookings.get(0); // Lấy booking đầu tiên khớp
            }
            newDurationMinutes = (oldBooking != null && oldBooking.getDurationMinutes() != null) ? oldBooking.getDurationMinutes() : 60;
            System.out.println("Duration giữ nguyên hoặc mặc định: " + newDurationMinutes + " phút");
        }


        if (timeChanged) { // Nếu thời gian hẹn thực sự thay đổi (ngày hoặc giờ)
            appointment.setAppointmentDate(newBookingStartInstant);
            appointment.setEndTime(newBookingStartInstant.plus(newDurationMinutes, ChronoUnit.MINUTES));
            System.out.println("Đã set AppointmentDate (UTC): " + appointment.getAppointmentDate());
            System.out.println("Đã set EndTime (UTC): " + appointment.getEndTime());
        }

        // Cập nhật User (nhân viên) nếu có thay đổi
        User staffToBook = appointment.getUser();
        boolean staffChanged = false;
        if (dto.getUserId() != null && (staffToBook == null || !dto.getUserId().equals(staffToBook.getId()))) {
            System.out.println("Phát hiện yêu cầu thay đổi User (nhân viên). Old User ID: " + (staffToBook != null ? staffToBook.getId() : "null") + ", New User ID: " + dto.getUserId());
            staffToBook = userRepository.findById(dto.getUserId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy User (nhân viên) với ID: " + dto.getUserId()));
            appointment.setUser(staffToBook);
            staffChanged = true;
            System.out.println("Đã cập nhật User sang ID: " + staffToBook.getId());
        }


        // 3. KIỂM TRA NHÂN VIÊN RẢNH (NẾU THỜI GIAN HOẶC NHÂN VIÊN HOẶC DỊCH VỤ THAY ĐỔI)
        boolean recheckStaffAvailability = timeChanged || staffChanged || serviceChanged;
        System.out.println("Cần kiểm tra lại lịch nhân viên không? " + recheckStaffAvailability);

        if (recheckStaffAvailability && staffToBook != null) {
            System.out.println("Đang kiểm tra lịch rảnh cho User ID: " + staffToBook.getId() +
                    " vào lúc (UTC): " + appointment.getAppointmentDate() +
                    " với duration: " + newDurationMinutes +
                    " (Loại trừ Appointment ID: " + appointmentId + ")");
            boolean staffIsActuallyAvailable = bookingService.isStaffAvailable(
                    staffToBook.getId(),
                    appointment.getAppointmentDate(),
                    newDurationMinutes,
                    appointmentId
            );
            if (!staffIsActuallyAvailable) {
                String localTimeDisplay = LocalDateTime.ofInstant(appointment.getAppointmentDate(), ZoneId.of("Asia/Ho_Chi_Minh"))
                        .format(DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy"));
                System.err.println("LỖI: Nhân viên " + staffToBook.getFullName() + " đã có lịch vào thời điểm này (" + localTimeDisplay + " giờ VN).");
                throw new RuntimeException("Nhân viên " + staffToBook.getFullName() +
                        " đã có lịch vào thời điểm này (" + localTimeDisplay +
                        "). Vui lòng chọn thời gian hoặc nhân viên khác.");
            }
            System.out.println("Kiểm tra OK: Nhân viên " + staffToBook.getFullName() + " rảnh.");
        }

        appointment.setUpdatedAt(Instant.now());
        System.out.println("Chuẩn bị lưu Appointment: " + appointment.toString()); // Giả sử Appointment có toString()
        Appointment savedAppointment = appointmentRepository.save(appointment);
        System.out.println("Đã lưu Appointment ID: " + savedAppointment.getId());

        // 4. CẬP NHẬT HOẶC VÔ HIỆU HÓA BOOKING LIÊN QUAN
        // ... (Logic vô hiệu hóa/cập nhật Booking giữ nguyên như trước, nhưng cần đảm bảo nó dùng đúng thông tin)

        boolean isFinalStatus = "completed".equalsIgnoreCase(newStatus) || "cancelled".equalsIgnoreCase(newStatus);
        System.out.println("Trạng thái cuối cùng (completed/cancelled)? " + isFinalStatus);
        System.out.println("Trạng thái có thực sự thay đổi sang final không? " +
                (isFinalStatus && !(oldStatus != null && (oldStatus.equalsIgnoreCase("completed") || oldStatus.equalsIgnoreCase("cancelled")))));

        if (isFinalStatus && !(oldStatus != null && (oldStatus.equalsIgnoreCase("completed") || oldStatus.equalsIgnoreCase("cancelled")))) {
            User currentStaff = savedAppointment.getUser();
            Instant currentAppointmentTime = savedAppointment.getAppointmentDate();
            org.aptech.backendmypham.models.Service currentService = savedAppointment.getService();

            if (currentStaff != null && currentAppointmentTime != null && currentService != null) {
                System.out.println("Đang tìm Booking để vô hiệu hóa: UserID=" + currentStaff.getId() +
                        ", ServiceID=" + currentService.getId() +
                        ", AppointmentTime(UTC)=" + currentAppointmentTime);

                List<Booking> bookingsToDeactivate = bookingRepository
                        .findByUserIdAndServiceIdAndBookingDateTimeAndIsActiveTrue(
                                currentStaff.getId(),
                                currentService.getId(),
                                currentAppointmentTime
                        );
                System.out.println("Tìm thấy " + bookingsToDeactivate.size() + " booking(s) để vô hiệu hóa.");

                for (Booking booking : bookingsToDeactivate) {
                    booking.setIsActive(false);
                    booking.setStatus(newStatus);
                    booking.setUpdatedAt(Instant.now());
                    bookingRepository.save(booking);
                    System.out.println("INFO: (Update) Đã vô hiệu hóa Booking ID: " + booking.getId() + " cho Appointment ID: " + savedAppointment.getId());
                }
            } else {
                System.out.println("WARN: Không đủ thông tin (User/Service/AppointmentTime) để tìm Booking cần vô hiệu hóa cho Appointment ID: " + savedAppointment.getId());
            }
        }
        System.out.println("---- Kết thúc updateAppointment cho ID: " + appointmentId + " ----");
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
