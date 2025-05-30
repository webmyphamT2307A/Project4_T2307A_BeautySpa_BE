package org.aptech.backendmypham.services.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.aptech.backendmypham.models.Attendance;
import org.aptech.backendmypham.models.User;
import org.aptech.backendmypham.repositories.AttendanceRepository;
import org.aptech.backendmypham.services.AttendanceService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {
    final private AttendanceRepository attendanceRepository;
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
}
