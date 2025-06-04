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
            Integer userId,
            Instant startTimeWindow,
            Instant endTimeWindow
    );

    List<Booking> findByUserIdAndServiceIdAndBookingDateTimeAndIsActiveTrue(
            Integer userId,
            Integer serviceId,
            Instant bookingDateTime
    );
}
