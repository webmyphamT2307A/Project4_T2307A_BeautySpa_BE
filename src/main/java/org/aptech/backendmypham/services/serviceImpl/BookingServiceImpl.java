package org.aptech.backendmypham.services.serviceImpl;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.aptech.backendmypham.dto.BookingDTO;
import org.aptech.backendmypham.models.Booking;
import org.aptech.backendmypham.repositories.BookingRepository;
import org.aptech.backendmypham.repositories.CustomerRepository;
import org.aptech.backendmypham.repositories.UserRepository;
import org.aptech.backendmypham.services.BookingService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@AllArgsConstructor
public class BookingServiceImpl implements BookingService {
    final private UserRepository userRepository;
    final private CustomerRepository customerRepository;
    private final org.aptech.backendmypham.repositories.ServiceRepository serviceRepository;
    private  final BookingRepository bookingRepository;
    @Override
    public boolean isStaffAvailable(Integer userId, Instant requestedStartTime, Integer durationMinutes) {
        // Optional: Kiểm tra xem userId có tồn tại không
        // User user = userRepository.findById(userId).orElse(null);
        // if (user == null) {
        //     throw new ResourceNotFoundException("User not found with id: " + userId); // Hoặc trả về false
        // }

        if (durationMinutes == null || durationMinutes <= 0) {
            throw new IllegalArgumentException("Thời lượng phải là số dương.");
        }

        Instant requestedEndTime = requestedStartTime.plus(durationMinutes, ChronoUnit.MINUTES);

        // Xác định khoảng thời gian rộng hơn để truy vấn DB hiệu quả
        // Ví dụ: lấy tất cả các booking của nhân viên trong ngày hôm đó
        Instant startOfDay = requestedStartTime.truncatedTo(ChronoUnit.DAYS);
        Instant endOfDay = startOfDay.plus(1, ChronoUnit.DAYS).minusNanos(1); // Gần cuối ngày

        // Lấy các booking hiện có của nhân viên trong ngày đó
        List<Booking> existingBookings = bookingRepository.findByUserIdAndIsActiveTrueAndBookingDateTimeBetween(
                userId,
                startOfDay, // Hoặc một khoảng thời gian hẹp hơn nếu muốn tối ưu
                endOfDay
        );

        // Kiểm tra xung đột với từng booking hiện có
        for (Booking existingBooking : existingBookings) {
            Instant existingBookingStartTime = existingBooking.getBookingDateTime();
            Integer existingDuration = existingBooking.getDurationMinutes();
            if (existingDuration == null || existingDuration <=0) continue; // Bỏ qua nếu booking không có thời lượng hợp lệ

            Instant existingBookingEndTime = existingBookingStartTime.plus(existingDuration, ChronoUnit.MINUTES);

            // Điều kiện xung đột: (StartA < EndB) and (EndA > StartB)
            // StartA = requestedStartTime, EndA = requestedEndTime
            // StartB = existingBookingStartTime, EndB = existingBookingEndTime
            boolean overlaps = requestedStartTime.isBefore(existingBookingEndTime) &&
                    requestedEndTime.isAfter(existingBookingStartTime);

            if (overlaps) {
                return false; // Nhân viên bận vì có lịch xung đột
            }
        }

        return true; // Nhân viên rảnh
    }

}
