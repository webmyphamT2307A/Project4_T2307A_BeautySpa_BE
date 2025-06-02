package org.aptech.backendmypham.services;

import org.aptech.backendmypham.dto.BookingDTO;

import java.time.Instant;

public interface BookingService {
    public boolean isStaffAvailable(Integer userId, Instant requestedStartTime, Integer durationMinutes);
    public void createBooking(BookingDTO bookingDTO);
}
