package org.aptech.backendmypham.repositories;

import org.aptech.backendmypham.models.Attendance;
import org.aptech.backendmypham.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Integer> {
    Optional<Attendance> findByUserAndCheckInBetween(User user, LocalDateTime start, LocalDateTime end);

    //count working days in a month
    @Query(nativeQuery = true, value = "SELECT COUNT(*) FROM attendance a " +
                    "WHERE a.user_id = ?1 " +
                    "AND EXTRACT(MONTH FROM a.check_in) = ?2 " +
                    "AND a.status = 'on_time'") // Assuming 'on_time' means worked day)
    long countWorkedDays(Long userId, Number month);

    //getTotalWorkdays
    @Query(nativeQuery = true, value = "SELECT COUNT(*) FROM attendance a " +
                    "WHERE a.user_id = ?1 " +
                    "AND EXTRACT(MONTH FROM a.check_in) = ?2 ")
    long getTotalWorkdays(Long userId, Number month);

    //sumTotalHours
    @Query(nativeQuery = true, value = "SELECT SUM(TIMESTAMPDIFF(SECOND, a.check_in, a.check_out) / 3600) " +
            "FROM attendance a " +
            "WHERE a.user_id = ?1 " +
            "AND EXTRACT(MONTH FROM a.check_in) = ?2 " +
            "AND a.status = 'on_time'")
    Long sumTotalHours(Long userId, int month);



}
