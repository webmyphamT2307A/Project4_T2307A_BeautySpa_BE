package org.aptech.backendmypham.services;

import org.aptech.backendmypham.models.Timeslots;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public interface TimeSlotService {
    public List<Timeslots> getALlTimeSlot();
    public int getAvailableSlot(LocalDate date, Long serviceId, Long timeSlotId);
}
