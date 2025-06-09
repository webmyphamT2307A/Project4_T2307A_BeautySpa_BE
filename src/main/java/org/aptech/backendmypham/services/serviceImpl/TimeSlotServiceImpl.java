package org.aptech.backendmypham.services.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.aptech.backendmypham.models.Timeslots;
import org.aptech.backendmypham.repositories.AppointmentRepository;
import org.aptech.backendmypham.repositories.TimeSlotsRepository;
import org.aptech.backendmypham.repositories.UserRepository;
import org.aptech.backendmypham.services.TimeSlotService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TimeSlotServiceImpl implements TimeSlotService {
    private final TimeSlotsRepository timeSlotsRepository;
    private final AppointmentRepository appointmentRepository;

    @Override
    public List<Timeslots> getALlTimeSlot(){
        return timeSlotsRepository.findAll();
    }
    @Override
    public int getAvailableSlot(LocalDate date, Long serviceId, Long timeSlotId) {
        int totalSlot = 10;
        ZoneId vietnamZoneId = ZoneId.of("Asia/Ho_Chi_Minh");
        Instant startOfDay = date.atStartOfDay(vietnamZoneId).toInstant();
        Instant endOfDay = date.plusDays(1).atStartOfDay(vietnamZoneId).toInstant();

        int booked = appointmentRepository.countByAppointmentDateBetweenAndServiceIdAndTimeSlot_SlotId(
                startOfDay, endOfDay, serviceId, timeSlotId
        );
        return Math.max(totalSlot - booked, 0);
    }
}
