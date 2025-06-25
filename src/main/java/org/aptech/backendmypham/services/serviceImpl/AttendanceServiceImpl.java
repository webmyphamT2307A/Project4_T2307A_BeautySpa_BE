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

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
=======
import java.util.*;
import java.util.stream.Collectors;


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
    public List<AttendanceHourDto> findByUserAndBetween(User user, LocalDateTime start, LocalDateTime end, String type) {
        List<Attendance> attendances = attendanceRepository.findByUserAndCheckInBetweenAndStatus(user, start, end);

        if ("month".equalsIgnoreCase(type)) {
            // Khởi tạo mảng 12 tháng (1-12)
            long[] totalHoursByMonth = new long[12];

            for (Attendance attendance : attendances) {
                LocalDateTime checkIn = attendance.getCheckIn();
                LocalDateTime checkOut = attendance.getCheckOut();
                if (checkIn != null && checkOut != null) {
                    long hoursWorked = Duration.between(checkIn, checkOut).toHours();
                    int month = checkIn.getMonthValue(); // 1 - 12
                    totalHoursByMonth[month - 1] += hoursWorked;
                }
            }

            List<AttendanceHourDto> result = new ArrayList<>();
            for (int i = 0; i < 12; i++) {
                result.add(new AttendanceHourDto(String.format("%02d", i + 1), totalHoursByMonth[i]));
            }
            return result;

        } else if ("year".equalsIgnoreCase(type)) {
            // Tính từ 5 năm trước đến hiện tại
            int currentYear = LocalDate.now().getYear();
            int startYear = currentYear - 4;
            Map<Integer, Long> yearToHours = new LinkedHashMap<>();
            for (int y = startYear; y <= currentYear; y++) {
                yearToHours.put(y, 0L);
            }

            for (Attendance attendance : attendances) {
                LocalDateTime checkIn = attendance.getCheckIn();
                LocalDateTime checkOut = attendance.getCheckOut();
                if (checkIn != null && checkOut != null) {
                    int year = checkIn.getYear();
                    if (yearToHours.containsKey(year)) {
                        long hoursWorked = Duration.between(checkIn, checkOut).toHours();
                        yearToHours.put(year, yearToHours.get(year) + hoursWorked);
                    }
                }
            }

            return yearToHours.entrySet().stream()
                    .map(entry -> new AttendanceHourDto(String.valueOf(entry.getKey()), entry.getValue()))
                    .collect(Collectors.toList());

        } else {
            // type = day hoặc default: xử lý theo tuần như cũ
            if (attendances.isEmpty()) {
                return List.of(
                        new AttendanceHourDto("T2", 0),
                        new AttendanceHourDto("T3", 0),
                        new AttendanceHourDto("T4", 0),
                        new AttendanceHourDto("T5", 0),
                        new AttendanceHourDto("T6", 0),
                        new AttendanceHourDto("T7", 0),
                        new AttendanceHourDto("CN", 0)
                );
            }

            long[] totalHours = new long[7]; // T2 đến CN
            for (Attendance attendance : attendances) {
                LocalDateTime checkIn = attendance.getCheckIn();
                LocalDateTime checkOut = attendance.getCheckOut();
                if (checkIn != null && checkOut != null) {
                    long hoursWorked = Duration.between(checkIn, checkOut).toHours();
                    int dayOfWeek = checkIn.getDayOfWeek().getValue(); // 1 = Monday, 7 = Sunday
                    totalHours[dayOfWeek - 1] += hoursWorked;
                }
            }

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


}
