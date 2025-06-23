package org.aptech.backendmypham.services.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.aptech.backendmypham.models.Timeslots;
import org.aptech.backendmypham.repositories.AppointmentRepository;
import org.aptech.backendmypham.repositories.TimeSlotsRepository;
import org.aptech.backendmypham.repositories.UsersScheduleRepository;
import org.aptech.backendmypham.services.TimeSlotService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TimeSlotServiceImpl implements TimeSlotService {
    private final TimeSlotsRepository timeSlotsRepository;
    private final AppointmentRepository appointmentRepository;
    private final UsersScheduleRepository usersScheduleRepository;

    @Override
    public List<Timeslots> getALlTimeSlot(){
        return timeSlotsRepository.findAll();
    }

    @Override
    public int getAvailableSlot(LocalDate date, Long serviceId, Long timeSlotId) {
        ZoneId vietnamZoneId = ZoneId.of("Asia/Ho_Chi_Minh");
        Instant startOfDay = date.atStartOfDay(vietnamZoneId).toInstant();
        Instant endOfDay = date.plusDays(1).atStartOfDay(vietnamZoneId).toInstant();

        // Get actual staff count who have schedules on this date
        int totalStaffWithSchedule = usersScheduleRepository.countStaffWithScheduleOnDate(date);

        // If no staff scheduled, return 0
        if (totalStaffWithSchedule == 0) {
            return 0;
        }

        // Count appointments already booked for this timeslot
        int booked = appointmentRepository.countByAppointmentDateBetweenAndServiceIdAndTimeSlot_SlotId(
                startOfDay, endOfDay, serviceId, timeSlotId
        );

        // Return available slots based on actual staff count
        return Math.max(totalStaffWithSchedule - booked, 0);
    }
} 