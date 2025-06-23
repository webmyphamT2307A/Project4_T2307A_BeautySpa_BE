package org.aptech.backendmypham.services.serviceImpl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.aptech.backendmypham.dto.*;
import org.aptech.backendmypham.models.*;
import org.aptech.backendmypham.repositories.*;
import org.aptech.backendmypham.services.AppointmentService;
import org.aptech.backendmypham.services.BookingService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.aptech.backendmypham.services.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {
    private static final Logger log = LoggerFactory.getLogger(AppointmentServiceImpl.class);
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final ServiceRepository serviceRepository;
    private final CustomerRepository customerRepository;
    private  final BookingService bookingService;
    private final BookingRepository bookingRepository;
    private final TimeSlotsRepository timeSlotsRepository;
    private final UsersScheduleRepository usersScheduleRepository;
    private final EmailService emailService;

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

        // 4. Xử lý TimeSlot


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



        // 6. KIỂM TRA LỊCH LÀM VIỆC VÀ LỊCH RẢNH CỦA NHÂN VIÊN
        if (appointment.getUser() != null) { // Chỉ kiểm tra nếu có nhân viên được chỉ định

            // ===== BƯỚC KIỂM TRA MỚI: Nhân viên có lịch làm việc (có ca) vào ngày này không? =====
            log.info("Kiểm tra lịch làm việc của nhân viên ID {} vào ngày {}", appointment.getUser().getId(), parsedDate);
            boolean isScheduledToWork = usersScheduleRepository.existsByUserAndWorkDateAndIsActiveTrue(
                    appointment.getUser(),
                    parsedDate // sử dụng LocalDate đã parse
            );

            if (!isScheduledToWork) {
                // Nếu không có lịch làm việc, bắn ra lỗi ngay lập tức
                throw new RuntimeException(
                        "Nhân viên " + appointment.getUser().getFullName() +
                                " không có lịch làm việc vào ngày " + dto.getAppointmentDate() +
                                ". Vui lòng chọn nhân viên hoặc ngày khác."
                );
            }
            log.info("Xác nhận: Nhân viên ID {} có lịch làm việc vào ngày {}. Tiếp tục kiểm tra booking.", appointment.getUser().getId(), parsedDate);
            // =========================================================================

            // Bước kiểm tra cũ: Nhân viên có bận (đã có booking khác) vào khung giờ này không?
            boolean staffIsActuallyAvailable = bookingService.isStaffAvailable(
                    appointment.getUser().getId(),
                    bookingStartInstant,
                    durationMinutes
            );
            if (!staffIsActuallyAvailable) {
                throw new RuntimeException("Nhân viên " + appointment.getUser().getFullName() + " đã có lịch vào thời điểm này. Vui lòng chọn thời gian hoặc nhân viên khác.");
            }
        }


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
        // 9.5. GỬI EMAIL XÁC NHẬN (LOGIC MỚI)
        // Chỉ gửi email nếu có thông tin email của khách hàng
        if (savedAppointment.getCustomer() != null && savedAppointment.getCustomer().getEmail() != null) {
            try {
                log.info("Chuẩn bị gửi email xác nhận cho lịch hẹn ID: {}", savedAppointment.getId());

                // Tạo đối tượng DTO cho request gửi mail
                EmailConfirmationRequestDto emailRequest = new EmailConfirmationRequestDto();

                // Lấy dữ liệu từ `savedAppointment` để điền vào emailRequest
                emailRequest.setAppointmentId(savedAppointment.getId());
                emailRequest.setCustomerName(savedAppointment.getFullName());
                emailRequest.setCustomerEmail(savedAppointment.getCustomer().getEmail());

                if (savedAppointment.getService() != null) {
                    emailRequest.setServiceName(savedAppointment.getService().getName());
                    // Chuyển BigDecimal thành float/double cho DTO
                    if(savedAppointment.getService().getPrice() != null) {
                        emailRequest.setPrice((double) savedAppointment.getService().getPrice().floatValue());
                    }
                }

                // Chuyển đổi Instant sang String cho DTO
                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy").withZone(ZoneId.of("Asia/Ho_Chi_Minh"));
                DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.of("Asia/Ho_Chi_Minh"));

                if(savedAppointment.getAppointmentDate() != null) {
                    // Truyền chuỗi nguyên gốc để EmailService tự xử lý
                    emailRequest.setAppointmentDate(savedAppointment.getAppointmentDate().toString());
                }

                if (savedAppointment.getTimeSlot() != null) {
                    emailRequest.setAppointmentTime(savedAppointment.getTimeSlot().getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")));
                    emailRequest.setEndTime(savedAppointment.getTimeSlot().getEndTime().format(DateTimeFormatter.ofPattern("HH:mm")));
                }

                if(savedAppointment.getUser() != null){
                    emailRequest.setStaffName(savedAppointment.getUser().getFullName());
                } else {
                    emailRequest.setStaffName("Sẽ được chỉ định sau");
                }



                emailRequest.setNotes(savedAppointment.getNotes());

                // Gọi service để gửi mail
                emailService.sendAppointmentConfirmation(emailRequest);

                log.info("Gửi email xác nhận thành công cho lịch hẹn ID: {}", savedAppointment.getId());

            } catch (Exception e) {
                // Quan trọng: Chỉ log lỗi, không ném exception ra ngoài
                // để không làm rollback giao dịch đã tạo lịch hẹn thành công.
                log.error("Lỗi khi gửi email xác nhận cho lịch hẹn ID: {} - Lỗi: {}", savedAppointment.getId(), e.getMessage());
            }
        } else {
            log.warn("Không thể gửi email cho lịch hẹn ID: {} do không có thông tin email khách hàng.", savedAppointment.getId());
        }


    }

    @Override
    public AppointmentResponseDto findByIdAndIsActive(Long id) {
        Appointment appointment = appointmentRepository.findByIdAndIsActive(id, true)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch hẹn"));

        return convertToDto(appointment);
    }

    // Sửa lại hàm này
    private AppointmentResponseDto convertToDto(Appointment appointment) {
        AppointmentResponseDto dto = new AppointmentResponseDto();
        dto.setId(appointment.getId());
        dto.setFullName(appointment.getFullName());
        dto.setPhoneNumber(appointment.getPhoneNumber());
        dto.setStatus(appointment.getStatus());
        dto.setSlot(appointment.getSlot());
        dto.setNotes(appointment.getNotes());
        // Luôn set giá trị, không bị ảnh hưởng bởi lỗi khác
        dto.setPrice(appointment.getPrice());

        // Kiểm tra null an toàn trước khi truy cập
        if (appointment.getAppointmentDate() != null) {
            dto.setAppointmentDate(appointment.getAppointmentDate().toString());
        }
        if (appointment.getEndTime() != null) {
            dto.setEndTime(appointment.getEndTime().toString());
        }

        // Xử lý Service
        if (appointment.getService() != null) {
            dto.setServiceName(appointment.getService().getName());
        } else {
            dto.setServiceName("N/A");
        }


        if (appointment.getCustomer() != null) {
            dto.setCustomerName(appointment.getCustomer().getFullName());
            dto.setCustomerEmail(appointment.getCustomer().getEmail());
            dto.setCustomerImageUrl(appointment.getCustomer().getImageUrl());
        } else {
            dto.setCustomerName("N/A");
            dto.setCustomerEmail(null);
            dto.setCustomerImageUrl(null);
        }

        // Xử lý User (Nhân viên)
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
        User staffToBook = appointment.getUser(); // Nhân viên hiện tại
        boolean staffChanged = false;

        if (dto.getUserId() != null) { // Nếu có User ID mới được cung cấp
            if (staffToBook == null || !dto.getUserId().equals(staffToBook.getId())) { // Nếu khác nhân viên cũ hoặc chưa có
                staffToBook = userRepository.findById(dto.getUserId())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy User (nhân viên) với ID: " + dto.getUserId()));
                appointment.setUser(staffToBook);
                staffChanged = true;
                System.out.println("Đã cập nhật User sang ID: " + staffToBook.getId());
            }
        } else { // Nếu dto.getUserId() là null -> nghĩa là "bỏ gán" (unassign)
            if (staffToBook != null) { // Chỉ đánh dấu thay đổi nếu trước đó có người được gán
                appointment.setUser(null);
                staffToBook = null; // Cập nhật staffToBook để logic kiểm tra lịch rảnh (nếu có) dùng đúng
                staffChanged = true;
                System.out.println("Đã bỏ gán User (nhân viên).");
            }
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

    public Map<String, Object> getAppointmentsGroupedByShift(LocalDate date, Long userId) {
        ZoneId zoneId = ZoneId.of("Asia/Ho_Chi_Minh");
        Instant startOfDay = date.atStartOfDay(zoneId).toInstant();
        Instant endOfDay = date.plusDays(1).atStartOfDay(zoneId).toInstant();
        List<Appointment> appointments = null;
        if (userId != null) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy User với ID: " + userId));
            appointments = appointmentRepository.findByAppointmentDateBetweenAndUser(startOfDay, endOfDay, user);
        } else {
            appointments = appointmentRepository.findByAppointmentDateBetween(startOfDay, endOfDay);
        }

        Map<String, List<Map<String, Object>>> groupedAppointments = appointments.stream()
                .collect(Collectors.groupingBy(appointment -> {
                    LocalTime startTime = appointment.getAppointmentDate().atZone(zoneId).toLocalTime();
                    return startTime.isBefore(LocalTime.NOON) ? "Sáng" : "Chiều";
                }, Collectors.mapping(appointment -> Map.of(
                        "id", "service-" + appointment.getId(),
                        "service", appointment.getService() != null ? appointment.getService().getName() : null,
                        "customerName", appointment.getCustomer() != null ? appointment.getCustomer().getFullName() : null,
                        "startTime", appointment.getAppointmentDate(),
                        "endTime", appointment.getEndTime(),
                        "timeDisplay", appointment.getSlot(),
                        "rating", appointment.getUser() != null ? appointment.getUser().getAverageRating() : null,
                        "commission", appointment.getPrice(),
                        "status", appointment.getStatus()
                ), Collectors.toList())));


        if (groupedAppointments.isEmpty()) {
            return Map.of();
        }

        return Map.of(
                "Sáng", groupedAppointments.getOrDefault("Sáng", List.of()),
                "Chiều", groupedAppointments.getOrDefault("Chiều", List.of())
        );
    }

    @Override
    public List<AppointmentResponseDto> getAppointmentsByUserId(Long userId) {
        List<Appointment> appointments = appointmentRepository.findAllByUserIdAndIsActive(userId);

        return appointments.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    // THÊM PHƯƠNG THỨC NÀY VÀO CUỐI FILE

    @Override
    public List<AppointmentHistoryDTO> getCustomerAppointmentHistory(Long customerId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Appointment> appointments = appointmentRepository.findByCustomerIdWithDetailsOrderByCreatedAtDesc(customerId, null, pageable);
        return appointments.getContent().stream()
                .map(this::convertToAppointmentHistoryDTO)
                .collect(Collectors.toList());
    }


    @Override
    public List<AppointmentHistoryDTO> getAppointmentHistoryByPhone(String phoneNumber) {
        List<Appointment> appointments = appointmentRepository.findByPhoneNumberWithDetailsOrderByCreatedAtDesc(phoneNumber, null);
        return appointments.stream()
                .map(this::convertToAppointmentHistoryDTO)
                .collect(Collectors.toList());
    }

    @Override
    public AppointmentStatsDTO getCustomerAppointmentStats(Long customerId) {
        List<Appointment> allAppointments = appointmentRepository.findByCustomerIdAndIsActive(customerId, null);

        Long total = (long) allAppointments.size();
        Long completed = allAppointments.stream().filter(apt -> "Đã hoàn thành".equals(determineStatusText(apt))).count();
        Long cancelled = allAppointments.stream().filter(apt -> "cancelled".equalsIgnoreCase(apt.getStatus())).count();
        Long upcoming = total - completed - cancelled;

        BigDecimal totalSpent = allAppointments.stream()
                .filter(apt -> "Đã hoàn thành".equals(determineStatusText(apt)))
                .map(Appointment::getPrice)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        String lastAppointmentDate = allAppointments.stream()
                .map(Appointment::getAppointmentDate)
                .max(Instant::compareTo)
                .map(instant -> instant.atZone(ZoneId.of("Asia/Ho_Chi_Minh")).format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .orElse("Chưa có");

        String mostUsedService = allAppointments.stream()
                .filter(apt -> apt.getService() != null)
                .collect(Collectors.groupingBy(apt -> apt.getService().getName(), Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Chưa có");

        return AppointmentStatsDTO.builder()
                .totalAppointments(total)
                .completedAppointments(completed)
                .cancelledAppointments(cancelled)
                .upcomingAppointments(upcoming > 0 ? upcoming : 0)
                .totalSpent(totalSpent)
                .lastAppointmentDate(lastAppointmentDate)
                .mostUsedService(mostUsedService)
                .build();
    }

    @Override
    @Transactional
    public void cancelAppointment(Long appointmentId) {
        log.info("Bắt đầu xử lý hủy cho lịch hẹn ID: {}", appointmentId);
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch hẹn với ID: " + appointmentId));

        if ("completed".equalsIgnoreCase(appointment.getStatus()) || "cancelled".equalsIgnoreCase(appointment.getStatus())) {
            throw new IllegalStateException("Không thể hủy lịch hẹn đã '" + appointment.getStatus() + "'.");
        }

        appointment.setStatus("cancelled");
        appointment.setIsActive(false); // Đặt lịch hẹn là không hoạt động
        appointment.setUpdatedAt(Instant.now());

        if (appointment.getUser() != null) {
            log.info("Giải phóng booking cho nhân viên ID {} lúc: {}", appointment.getUser().getId(), appointment.getAppointmentDate());
            List<Booking> relatedBookings = bookingRepository.findByUserIdAndBookingDateTimeAndIsActiveTrue(
                    appointment.getUser().getId(), appointment.getAppointmentDate());
            relatedBookings.forEach(booking -> {
                booking.setStatus("cancelled");
                booking.setIsActive(false);
                booking.setUpdatedAt(Instant.now());
                bookingRepository.save(booking);
            });
        }

        Appointment cancelledAppointment = appointmentRepository.save(appointment);
        log.info("Đã hủy thành công lịch hẹn ID: {}. Trạng thái mới: {}, Active: {}",
                appointmentId, cancelledAppointment.getStatus(), cancelledAppointment.getIsActive());

        if (cancelledAppointment.getCustomer() != null && cancelledAppointment.getCustomer().getEmail() != null) {
            try {
                emailService.sendAppointmentCancellation(createEmailRequest(cancelledAppointment));
            } catch (Exception e) {
                log.error("Gửi email hủy thất bại cho lịch hẹn ID: {}. Lỗi: {}", cancelledAppointment.getId(), e.getMessage(), e);
            }
        }
    }



    private AppointmentHistoryDTO convertToAppointmentHistoryDTO(Appointment appointment) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy").withZone(ZoneId.of("Asia/Ho_Chi_Minh"));
        return AppointmentHistoryDTO.builder()
                .id(appointment.getId())
                .appointmentId(appointment.getId())
                .customerId(appointment.getCustomer() != null ? appointment.getCustomer().getId() : null)
                .customerName(appointment.getFullName())
                .customerPhone(appointment.getPhoneNumber())
                .customerEmail(appointment.getCustomer() != null ? appointment.getCustomer().getEmail() : null)
                .serviceId(appointment.getService() != null ? (long) appointment.getService().getId() : null)
                .serviceName(appointment.getService() != null ? appointment.getService().getName() : "N/A")
                .servicePrice(appointment.getPrice())
                .serviceDuration(appointment.getService() != null && appointment.getService().getDuration() != null ?
                        appointment.getService().getDuration() : 60)
                .userId(appointment.getUser() != null ? appointment.getUser().getId() : null)
                .userName(appointment.getUser() != null ? appointment.getUser().getFullName() : "N/A")
                .userImageUrl(appointment.getUser() != null ? appointment.getUser().getImageUrl() : null)
                .userRating(appointment.getUser() != null ? appointment.getUser().getAverageRating() : null)
                .appointmentDate(dateFormatter.format(appointment.getAppointmentDate()))
                .appointmentTime(appointment.getSlot())
                .slot(appointment.getSlot())
                .status(appointment.getStatus())
                .notes(appointment.getNotes())
                .isActive(appointment.getIsActive())
                .createdAt(appointment.getCreatedAt().toString())
                .statusText(determineStatusText(appointment))
                .statusClassName(determineStatusClassName(appointment))
                .canCancel(canCancelAppointment(appointment))
                .displayDate(DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy", new Locale("vi", "VN"))
                        .withZone(ZoneId.of("Asia/Ho_Chi_Minh"))
                        .format(appointment.getAppointmentDate()))
                .build();
    }
    private String determineStatusText(Appointment appointment) {
        // PRIORITY 1: Check explicit status first
        if (appointment.getStatus() != null) {
            String status = appointment.getStatus().toLowerCase().trim();
            if (status.contains("cancel")) {
                return "Đã hủy";
            }
            if ("completed".equals(status)) {
                return "Đã hoàn thành";
            }
        }

        // PRIORITY 2: Date logic
        // Lấy ngày hiện tại và ngày hẹn
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        LocalDate aptDate = appointment.getAppointmentDate()
                .atZone(ZoneId.of("Asia/Ho_Chi_Minh"))
                .toLocalDate();

        // Nếu lịch hẹn đã qua, coi như đã hoàn thành
        // (Logic này vẫn giữ nguyên vì một lịch hẹn "pending" trong quá khứ thì cũng nên là "hoàn thành")
        if (aptDate.isBefore(today)) {
            return "Đã hoàn thành";
        }

        // Nếu không, xác định là hôm nay hay sắp tới
        if (aptDate.isEqual(today)) {
            return "Hôm nay";
        } else {
            return "Sắp tới";
        }
    }

    private String determineStatusClassName(Appointment appointment) {
        // PRIORITY 1: Check explicit status first
        if (appointment.getStatus() != null) {
            String status = appointment.getStatus().toLowerCase().trim();

            if (status.contains("cancel")) {
                return "bg-danger";
            }

            if ("completed".equals(status)) {
                return "bg-success";
            }
        }

        // PRIORITY 2: Date logic
        LocalDate today = LocalDate.now();
        LocalDate aptDate = appointment.getAppointmentDate().atZone(ZoneId.of("Asia/Ho_Chi_Minh")).toLocalDate();

        if (aptDate.isBefore(today)) {
            return "bg-success";
        } else if (aptDate.isEqual(today)) {
            return "bg-warning text-dark";
        } else {
            return "bg-info";
        }
    }


    @Override
    public List<AppointmentResponseDto> getALlAppointment() {
        List<Appointment> appointments = appointmentRepository.findAll();
        appointments.sort((a1, a2) -> a2.getId().compareTo(a1.getId()));

        return appointments.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    private Boolean canCancelAppointment(Appointment appointment) {
        String statusText = determineStatusText(appointment);
        return !"Đã hủy".equals(statusText) && !"Đã hoàn thành".equals(statusText);
    }
    private EmailConfirmationRequestDto createEmailRequest(Appointment appointment) {
        EmailConfirmationRequestDto emailRequest = new EmailConfirmationRequestDto();
        emailRequest.setAppointmentId(appointment.getId());
        emailRequest.setCustomerName(appointment.getFullName());

        if (appointment.getCustomer() != null) {
            emailRequest.setCustomerEmail(appointment.getCustomer().getEmail());
        }
        if (appointment.getService() != null) {
            emailRequest.setServiceName(appointment.getService().getName());
            if (appointment.getService().getPrice() != null) {
                emailRequest.setPrice(appointment.getService().getPrice().doubleValue());
            }
        }
        if (appointment.getAppointmentDate() != null) {
            emailRequest.setAppointmentDate(appointment.getAppointmentDate().toString());
        }
        if (appointment.getTimeSlot() != null) {
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
            emailRequest.setAppointmentTime(appointment.getTimeSlot().getStartTime().format(timeFormatter));
            emailRequest.setEndTime(appointment.getTimeSlot().getEndTime().format(timeFormatter));
        }
        if (appointment.getUser() != null) {
            emailRequest.setStaffName(appointment.getUser().getFullName());
        } else {
            emailRequest.setStaffName("Sẽ được chỉ định sau");
        }

        emailRequest.setNotes(appointment.getNotes());
        return emailRequest;
    }
}
