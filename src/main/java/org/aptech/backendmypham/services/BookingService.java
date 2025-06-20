package org.aptech.backendmypham.services;

import org.aptech.backendmypham.dto.BookingDTO;

import java.time.Instant;

public interface BookingService {
    public boolean isStaffAvailable(Long userId, Instant requestedStartTime, Integer durationMinutes, Long appointmentIdToExclude);
    public boolean isStaffAvailable(Long userId, Instant requestedStartTime, Integer durationMinutes);
}
