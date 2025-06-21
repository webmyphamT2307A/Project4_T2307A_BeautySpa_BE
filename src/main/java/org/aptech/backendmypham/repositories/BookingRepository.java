package org.aptech.backendmypham.repositories;

import org.aptech.backendmypham.models.Booking;
import org.aptech.backendmypham.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

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

    @Query(value = "SELECT * FROM bookings b WHERE b.user_id = ?1 " +
            "AND MONTH(b.booking_date_time) = ?2 " +
            "AND YEAR(b.booking_date_time) = ?3 " +
            "AND b.is_active = true", nativeQuery = true)
    List<Booking> findBookingsByUserIdAndMonth(Long userId, int month, int year);


    //findBookingsByUserIdAndMonthAndYear theo hql
    @Query("SELECT b FROM Booking b WHERE b.user.id = :userId " +
            "AND MONTH(b.bookingDateTime) = :month " +
            "AND YEAR(b.bookingDateTime) = :year " +
            "AND b.isActive = true")
    List<Booking> findBookingsByUserIdAndMonthAndYear(
            @Param("userId") Long userId,
            @Param("month") int month,
            @Param("year") int year
    );
    List<Booking> findByUserIdAndServiceIdAndBookingDateTimeAndIsActiveTrue(
            Long userId,
            Integer serviceId,
            Instant bookingDateTime
    );
//    List<Booking> findByUserIdAndServiceIdAndBookingDateTimeAndIsActiveTrue(
//            Integer userId,
//            Integer serviceId,
//            Instant bookingDateTime
//    );
    List<Booking> findByUserAndIsActiveTrue(User user);

    // Tìm tất cả booking của một user, không phân biệt trạng thái
    List<Booking> findByUser(User user);

    List<Booking> findByUserIdAndBookingDateTimeAndIsActiveTrue(Long id, Instant appointmentDate);
}