package org.aptech.backendmypham.services.serviceImpl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.aptech.backendmypham.dto.AttendanceDTO;
import org.aptech.backendmypham.dto.AttendanceHourDto;
import org.aptech.backendmypham.dto.UsersScheduleResponseDto;
import org.aptech.backendmypham.exception.ResourceNotFoundException;
import org.aptech.backendmypham.models.Attendance;
import org.aptech.backendmypham.models.User;
import org.aptech.backendmypham.models.UsersSchedule;
import org.aptech.backendmypham.repositories.AttendanceRepository;
import org.aptech.backendmypham.repositories.UserRepository;
import org.aptech.backendmypham.repositories.UsersScheduleRepository;
import org.aptech.backendmypham.services.AttendanceService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {
    final private AttendanceRepository attendanceRepository;
    final private UsersScheduleRepository usersScheduleRepository;
    final private UserRepository userRepository;
    @Override
    public List<Attendance> getAll(){
        return attendanceRepository.findAll();
    }
    @Override
    public Attendance save(Attendance attendance) {
        return attendanceRepository.save(attendance);
    }
    @Override
    public Optional<Attendance> findByUserAndCheckInBetween(User user, LocalDateTime start, LocalDateTime end) {
        return attendanceRepository.findByUserAndCheckInBetween(user, start, end);
    }

    @Override
    public List<AttendanceHourDto> findByUserAndBetween(User user, LocalDateTime start, LocalDateTime end) {
        List<Attendance> attendances = attendanceRepository.findByUserAndCheckInBetweenAndStatus(user, start, end);
        if(attendances.isEmpty()) {
            return List.of(
                    new AttendanceHourDto("T2", 0),
                    new AttendanceHourDto("T3", 0),
                    new AttendanceHourDto("T4", 0),
                    new AttendanceHourDto("T5", 0),
                    new AttendanceHourDto("T6", 0),
                    new AttendanceHourDto("T7", 0),
                    new AttendanceHourDto("CN", 0));
        }
        // dùng vòng lặp để tính giờ làm việc trong tuần và trả ra T2, T3, T4, T5, T6, T7, CN
        long[] totalHours = new long[7]; // Mảng để lưu tổng giờ làm việc cho từng ngày trong tuần
        for (Attendance attendance : attendances) {
            LocalDateTime checkIn = attendance.getCheckIn();
            LocalDateTime checkOut = attendance.getCheckOut();
            if (checkIn != null && checkOut != null) {
                long hoursWorked = java.time.Duration.between(checkIn, checkOut).toHours();
                int dayOfWeek = checkIn.getDayOfWeek().getValue(); // 1 = Monday, 7 = Sunday
                totalHours[dayOfWeek - 1] += hoursWorked; // Cộng giờ làm việc vào ngày tương ứng
            }
        }
        // Chuyển đổi mảng tổng giờ làm việc thành danh sách AttendanceHourDto
        return List.of(
                new AttendanceHourDto("T2", totalHours[0]),
                new AttendanceHourDto("T3", totalHours[1]),
                new AttendanceHourDto("T4", totalHours[2]),
                new AttendanceHourDto("T5", totalHours[3]),
                new AttendanceHourDto("T6", totalHours[4]),
                new AttendanceHourDto("T7", totalHours[5]),
                new AttendanceHourDto("CN", totalHours[6])
        );
    }




}
