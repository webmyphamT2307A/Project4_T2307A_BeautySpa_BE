package org.aptech.backendmypham.services.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.aptech.backendmypham.models.Attendance;
import org.aptech.backendmypham.repositories.AttendanceRepository;
import org.aptech.backendmypham.services.AttendanceService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {
    final private AttendanceRepository attendanceRepository;
    @Override
    public List<Attendance> getAll(){
        return attendanceRepository.findAll();
    }
}
