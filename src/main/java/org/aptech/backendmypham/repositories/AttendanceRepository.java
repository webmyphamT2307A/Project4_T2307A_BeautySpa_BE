package org.aptech.backendmypham.repositories;

import org.aptech.backendmypham.models.Attendance;
import org.aptech.backendmypham.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Integer> {
    Optional<Attendance> findByUserAndCheckInBetween(User user, LocalDateTime start, LocalDateTime end);
}
