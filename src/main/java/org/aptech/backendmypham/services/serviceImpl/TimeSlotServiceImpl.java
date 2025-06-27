package org.aptech.backendmypham.services.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.aptech.backendmypham.dto.TimeSlotDTO;
import org.aptech.backendmypham.exception.ResourceNotFoundException;
import org.aptech.backendmypham.models.Timeslots;
import org.aptech.backendmypham.repositories.AppointmentRepository;
import org.aptech.backendmypham.repositories.TimeSlotsRepository;
import org.aptech.backendmypham.repositories.UsersScheduleRepository;
import org.aptech.backendmypham.services.TimeSlotService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
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
        final int TOTAL_SLOTS = 10;

        ZoneId vietnamZoneId = ZoneId.of("Asia/Ho_Chi_Minh");
        Instant startOfDay = date.atStartOfDay(vietnamZoneId).toInstant();
        Instant endOfDay = date.plusDays(1).atStartOfDay(vietnamZoneId).toInstant();

        // Đếm số lịch hẹn đã được đặt cho ngày và khung giờ này
        // Lưu ý: Bạn cần có phương thức count... trong AppointmentRepository
        long bookedSlots = appointmentRepository.countByAppointmentDateBetweenAndTimeSlot_SlotIdAndIsActiveTrue(
                startOfDay,
                endOfDay,
                timeSlotId
        );

        int availableSlots = TOTAL_SLOTS - (int) bookedSlots;

        // Đảm bảo số slot trống không bao giờ là số âm
        return Math.max(0, availableSlots);
    }

    // ✅ NEW CRUD IMPLEMENTATIONS
    @Override
    public Timeslots createTimeSlot(TimeSlotDTO timeSlotDTO) {
        Timeslots newTimeSlot = new Timeslots();
        newTimeSlot.setStartTime(timeSlotDTO.getStartTime());
        newTimeSlot.setEndTime(timeSlotDTO.getEndTime());
        newTimeSlot.setShift(timeSlotDTO.getShift());
        newTimeSlot.setIsActive(true);
        newTimeSlot.setCreatedAt(LocalDateTime.now());
        return timeSlotsRepository.save(newTimeSlot);
    }

    @Override
    public Timeslots updateTimeSlot(Long slotId, TimeSlotDTO timeSlotDTO) {
        Timeslots existingTimeSlot = timeSlotsRepository.findById(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy TimeSlot với ID: " + slotId));

        existingTimeSlot.setStartTime(timeSlotDTO.getStartTime());
        existingTimeSlot.setEndTime(timeSlotDTO.getEndTime());
        existingTimeSlot.setShift(timeSlotDTO.getShift());

        return timeSlotsRepository.save(existingTimeSlot);
    }

    @Override
    public void deleteTimeSlot(Long slotId) {
        Timeslots timeSlot = timeSlotsRepository.findById(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy TimeSlot với ID: " + slotId));
        // Soft delete
        timeSlot.setIsActive(false);
        timeSlotsRepository.save(timeSlot);
    }
    @Override
    public Timeslots getTimeSlotById(Long slotId) {
        return timeSlotsRepository.findById(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy TimeSlot với ID: " + slotId));
    }
    @Override
    public int getTotalStaffScheduled(LocalDate date) {
        return usersScheduleRepository.countDistinctUserByWorkDateAndIsActiveTrue(date);
    }
} 