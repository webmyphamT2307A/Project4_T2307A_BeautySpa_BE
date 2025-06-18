package org.aptech.backendmypham.repositories;

import org.aptech.backendmypham.models.Booking;
import org.aptech.backendmypham.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUserIdAndIsActiveTrueAndBookingDateTimeBetween(
            Long userId,
            Instant startTimeWindow,
            Instant endTimeWindow
    );
    // Phương thức mới để tìm booking xung đột, có loại trừ
    @Query("SELECT b FROM Booking b WHERE b.user.id = :userId " +
            "AND b.isActive = true " +
            "AND b.bookingDateTime BETWEEN :startTimeWindow AND :endTimeWindow " +
            "AND (b.appointment IS NULL OR b.appointment.id != :appointmentIdToExclude)")
    List<Booking> findConflictingBookingsWithExclusion(
            @Param("userId") Long userId,
            @Param("startTimeWindow") Instant startTimeWindow,
            @Param("endTimeWindow") Instant endTimeWindow,
            @Param("appointmentIdToExclude") Long appointmentIdToExclude
    );

    // Các phương thức bạn đã có để tìm booking theo User, Service, DateTime
    List<Booking> findByUserIdAndServiceIdAndBookingDateTimeAndIsActiveTrue(
            Long userId,
            Integer serviceId, // Hoặc Integer tùy kiểu ID của Service entity
            Instant bookingDateTime
    );

    List<Booking> findByUserIdAndBookingDateTimeAndIsActiveTrue(Long id, Instant appointmentDate);

//    List<Booking> findByUserIdAndServiceIdAndBookingDateTimeAndIsActiveTrue(
//            Integer userId,
//            Integer serviceId,
//            Instant bookingDateTime
//    );
}
