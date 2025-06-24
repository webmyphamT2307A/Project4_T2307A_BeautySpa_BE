package org.aptech.backendmypham.services;

import org.aptech.backendmypham.dto.AttendanceHourDto;
import org.aptech.backendmypham.models.Attendance;
import org.aptech.backendmypham.models.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AttendanceService {
    public List<Attendance> getAll();
    public Attendance save(Attendance attendance);
    Optional<Attendance> findByUserAndCheckInBetween(User user, LocalDateTime start, LocalDateTime end);

    List<AttendanceHourDto> findByUserAndBetween(User user, LocalDateTime start, LocalDateTime end, String type);
}
