package org.aptech.backendmypham.repositories;

import org.aptech.backendmypham.models.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttendanceRepository extends JpaRepository<Attendance, Integer> {
}
