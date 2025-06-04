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
    public boolean isStaffAvailable(Integer userId, Instant requestedStartTime, Integer durationMinutes, Long appointmentIdToExclude) {
        if (durationMinutes == null || durationMinutes <= 0) {
            throw new IllegalArgumentException("Thời lượng phải là số dương.");
        }

        Instant requestedEndTime = requestedStartTime.plus(durationMinutes, ChronoUnit.MINUTES);
        Instant startOfDay = requestedStartTime.truncatedTo(ChronoUnit.DAYS);
        Instant endOfDay = startOfDay.plus(1, ChronoUnit.DAYS).minusNanos(1);

        // Lấy các booking hiện có, loại trừ booking của appointmentIdToExclude (nếu được cung cấp)
        List<Booking> existingBookings;
        if (appointmentIdToExclude != null) {
            // Bạn cần một phương thức repository tùy chỉnh ở đây
            existingBookings = bookingRepository.findConflictingBookingsWithExclusion(
                    userId,
                    startOfDay,
                    endOfDay,
                    appointmentIdToExclude
            );
        } else {
            existingBookings = bookingRepository.findByUserIdAndIsActiveTrueAndBookingDateTimeBetween(
                    userId,
                    startOfDay,
                    endOfDay
            );
        }

        // System.out.println("DEBUG: isStaffAvailable (exclude " + appointmentIdToExclude + ") found " + existingBookings.size() + " bookings to check.");

        for (Booking existingBooking : existingBookings) {
            // Nếu dùng query đã loại trừ rồi thì không cần check lại existingBooking.getAppointment().getId().equals(appointmentIdToExclude)
            // Tuy nhiên, nếu query không loại trừ được ở mức DB, bạn có thể lọc ở đây:
            // if (appointmentIdToExclude != null && existingBooking.getAppointment() != null && existingBooking.getAppointment().getId().equals(appointmentIdToExclude)) {
            //     continue; // Bỏ qua booking của chính appointment đang được cập nhật
            // }

            Instant existingBookingStartTime = existingBooking.getBookingDateTime();
            Integer existingDuration = existingBooking.getDurationMinutes();
            if (existingDuration == null || existingDuration <= 0) continue;

            Instant existingBookingEndTime = existingBookingStartTime.plus(existingDuration, ChronoUnit.MINUTES);

            boolean overlaps = requestedStartTime.isBefore(existingBookingEndTime) &&
                    requestedEndTime.isAfter(existingBookingStartTime);

            if (overlaps) {
                // System.out.println("DEBUG: Overlap detected with Booking ID: " + existingBooking.getId());
                return false; // Nhân viên bận
            }
        }
        return true; // Nhân viên rảnh
    }

    // Triển khai phương thức cũ (gọi phương thức mới với exclusion là null)
    @Override
    public boolean isStaffAvailable(Integer userId, Instant requestedStartTime, Integer durationMinutes) {
        return this.isStaffAvailable(userId, requestedStartTime, durationMinutes, null); // Gọi hàm mới, không loại trừ appointment nào
    }

}
