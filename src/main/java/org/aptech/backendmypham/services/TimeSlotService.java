package org.aptech.backendmypham.services;

import org.aptech.backendmypham.dto.TimeSlotDTO;
import org.aptech.backendmypham.models.Timeslots;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public interface TimeSlotService {
    public List<Timeslots> getALlTimeSlot();
    public int getAvailableSlot(LocalDate date, Long serviceId, Long timeSlotId);

    Timeslots createTimeSlot(TimeSlotDTO timeSlotDTO);
    Timeslots updateTimeSlot(Long slotId, TimeSlotDTO timeSlotDTO);
    void deleteTimeSlot(Long slotId);
    Timeslots getTimeSlotById(Long slotId);
    int getTotalStaffScheduled(LocalDate date);
}
