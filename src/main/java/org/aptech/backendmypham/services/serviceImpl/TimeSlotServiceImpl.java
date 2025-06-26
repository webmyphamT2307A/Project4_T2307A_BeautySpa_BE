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
        // 1. Lấy danh sách ID của TẤT CẢ nhân viên có lịch làm trong ngày hôm đó
        List<Long> allScheduledStaffIds = usersScheduleRepository.findUserIdsByWorkDateAndIsActiveTrue(date);

        // Nếu không có nhân viên nào làm việc, trả về 0 slot trống
        if (allScheduledStaffIds.isEmpty()) {
            return 0;
        }

        // 2. Lấy danh sách ID của những nhân viên đã BẬN vào khung giờ đó
        ZoneId vietnamZoneId = ZoneId.of("Asia/Ho_Chi_Minh");
        Instant startOfDay = date.atStartOfDay(vietnamZoneId).toInstant();
        Instant endOfDay = date.plusDays(1).atStartOfDay(vietnamZoneId).toInstant();

        List<Long> busyStaffIds = appointmentRepository.findBusyUserIdsByDateAndTimeSlot(startOfDay, endOfDay, timeSlotId);

        // 3. Tìm những nhân viên RẢNH bằng cách loại bỏ những người đã bận
        // Tạo một bản sao để thực hiện thao tác removeAll
        List<Long> availableStaffIds = new ArrayList<>(allScheduledStaffIds);
        availableStaffIds.removeAll(busyStaffIds);

        // 4. Số slot trống chính là số lượng nhân viên còn lại trong danh sách rảnh
        return availableStaffIds.size();
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