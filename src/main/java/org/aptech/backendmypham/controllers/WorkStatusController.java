package org.aptech.backendmypham.controllers;

import org.aptech.backendmypham.dto.WorkStatusResponse;
import org.aptech.backendmypham.exception.ResourceNotFoundException;
import org.aptech.backendmypham.models.User;
import org.aptech.backendmypham.models.UsersSchedule;
import org.aptech.backendmypham.models.Schedule;
import org.aptech.backendmypham.repositories.UserRepository;
import org.aptech.backendmypham.repositories.UsersScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/work-status")
public class WorkStatusController {

    @Autowired
    private UsersScheduleRepository usersScheduleRepository;

    @Autowired
    private UserRepository userRepository;
    @GetMapping("/today")
    public ResponseEntity<WorkStatusResponse> getWorkStatus(@RequestParam Long userId) {
        LocalDate today = LocalDate.now();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy User với ID: " + userId));

        List<UsersSchedule> schedules = usersScheduleRepository.findByUserAndWorkDateAndIsActiveTrue(user, today);

        boolean hasShift = !schedules.isEmpty();
        String currentStatus = hasShift ? "Working" : "No Shift";
        String message = hasShift ? "Bạn có ca làm việc hôm nay" : "Không có ca làm việc hôm nay";

        List<Schedule> scheduleResponses = schedules.stream().map(schedule -> new Schedule(
                schedule.getId(),
                schedule.getWorkDate(),
                schedule.getWorkDate().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                schedule.getWorkDate().getDayOfWeek().toString(),
                schedule.getShift(),
                schedule.getCheckInTime() != null ? schedule.getCheckInTime().toString() : null,
                schedule.getCheckOutTime() != null ? schedule.getCheckOutTime().toString() : null,
                schedule.getStatus(),
                schedule.getIsActive()
        )).collect(Collectors.toList());

        WorkStatusResponse response = new WorkStatusResponse(
                hasShift,
                scheduleResponses,
                currentStatus,
                message
        );

        return ResponseEntity.ok(response);
    }
}