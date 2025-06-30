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
    @Transactional // Sử dụng @Transactional để đảm bảo cả hai thao tác (lưu attendance và cập nhật schedule) thành công hoặc thất bại cùng nhau
    public Attendance save(Attendance attendance) {
        // 1. Lưu bản ghi điểm danh như bình thường
        Attendance savedAttendance = attendanceRepository.save(attendance);
        return savedAttendance;
    }
    @Override
    public Optional<Attendance> findByUserAndCheckInBetween(User user, LocalDateTime start, LocalDateTime end) {
        return attendanceRepository.findByUserAndCheckInBetween(user, start, end);
    }

    @Override
    public List<AttendanceHourDto> findByUserAndBetween(User user, LocalDateTime start, LocalDateTime end, String type) {
        List<Attendance> attendances = attendanceRepository.findByUserAndCheckInBetweenAndStatus(user, start, end);

        if ("month".equalsIgnoreCase(type)) {
            double[] totalHoursByMonth = new double[12];

            for (Attendance attendance : attendances) {
                LocalDateTime checkIn = attendance.getCheckIn();
                LocalDateTime checkOut = attendance.getCheckOut();
                if (checkIn != null && checkOut != null) {
                    double hoursWorked = Duration.between(checkIn, checkOut).toMinutes() / 60.0;
                    int month = checkIn.getMonthValue();
                    totalHoursByMonth[month - 1] += hoursWorked;
                }
            }

            List<AttendanceHourDto> result = new ArrayList<>();
            for (int i = 0; i < 12; i++) {
                double rounded = Math.round(totalHoursByMonth[i] * 10) / 10.0;
                result.add(new AttendanceHourDto(String.format("%02d", i + 1), rounded));
            }
            return result;

        } else if ("year".equalsIgnoreCase(type)) {
            int currentYear = LocalDate.now().getYear();
            int startYear = currentYear - 4;
            Map<Integer, Double> yearToHours = new LinkedHashMap<>();
            for (int y = startYear; y <= currentYear; y++) {
                yearToHours.put(y, 0.0);
            }

            for (Attendance attendance : attendances) {
                LocalDateTime checkIn = attendance.getCheckIn();
                LocalDateTime checkOut = attendance.getCheckOut();
                if (checkIn != null && checkOut != null) {
                    int year = checkIn.getYear();
                    if (yearToHours.containsKey(year)) {
                        double hoursWorked = Duration.between(checkIn, checkOut).toMinutes() / 60.0;
                        yearToHours.put(year, yearToHours.get(year) + hoursWorked);
                    }
                }
            }

            return yearToHours.entrySet().stream()
                    .map(entry -> {
                        double rounded = Math.round(entry.getValue() * 10) / 10.0;
                        return new AttendanceHourDto(String.valueOf(entry.getKey()), rounded);
                    })
                    .collect(Collectors.toList());

        } else {
            double[] totalHours = new double[7]; // T2 - CN

            for (Attendance attendance : attendances) {
                LocalDateTime checkIn = attendance.getCheckIn();
                LocalDateTime checkOut = attendance.getCheckOut();
                if (checkIn != null && checkOut != null) {
                    double hoursWorked = Duration.between(checkIn, checkOut).toMinutes() / 60.0;
                    int dayOfWeek = checkIn.getDayOfWeek().getValue(); // 1 = Monday, 7 = Sunday
                    totalHours[dayOfWeek - 1] += hoursWorked;
                }
            }

            return List.of(
                    new AttendanceHourDto("T2", Math.round(totalHours[0] * 10) / 10.0),
                    new AttendanceHourDto("T3", Math.round(totalHours[1] * 10) / 10.0),
                    new AttendanceHourDto("T4", Math.round(totalHours[2] * 10) / 10.0),
                    new AttendanceHourDto("T5", Math.round(totalHours[3] * 10) / 10.0),
                    new AttendanceHourDto("T6", Math.round(totalHours[4] * 10) / 10.0),
                    new AttendanceHourDto("T7", Math.round(totalHours[5] * 10) / 10.0),
                    new AttendanceHourDto("CN", Math.round(totalHours[6] * 10) / 10.0)
            );
        }
    }


}
