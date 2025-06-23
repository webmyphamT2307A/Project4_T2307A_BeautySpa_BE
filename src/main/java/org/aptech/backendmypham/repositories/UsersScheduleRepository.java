package org.aptech.backendmypham.repositories;

import org.aptech.backendmypham.models.User;
import org.aptech.backendmypham.models.UsersSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository; // Thêm @Repository cho rõ ràng

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository // Thêm annotation @Repository
public interface UsersScheduleRepository extends JpaRepository<UsersSchedule, Integer> { // Sửa Long thành Integer

    // Tìm lịch trình theo User và trong một khoảng ngày làm việc, chỉ lấy các bản ghi active
    List<UsersSchedule> findByUserAndWorkDateBetweenAndIsActiveTrue(User user, LocalDate startDate, LocalDate endDate);

    // Tìm tất cả lịch trình active của một User
    List<UsersSchedule> findByUserAndIsActiveTrue(User user);

    // Tìm một lịch trình cụ thể bằng ID và phải đang active
    // Kiểu ID ở đây là Integer, khớp với khóa chính của UsersSchedule
    Optional<UsersSchedule> findByIdAndIsActiveTrue(Integer id);

    // Tìm tất cả các lịch trình đang active (đây là phương thức chuẩn và nên được sử dụng)
    List<UsersSchedule> findByIsActiveTrue();

    // Phương thức findAllActive() đã bị xóa vì findByIsActiveTrue() đã có chức năng tương tự.
    // Nếu bạn vẫn muốn giữ findAllActive(), hãy thêm @Query như sau:
    // @Query("SELECT us FROM UsersSchedule us WHERE us.isActive = true")
    // List<UsersSchedule> findAllActive();

   List<UsersSchedule> findByUserAndWorkDateAndIsActiveTrue(User user, LocalDate workDate);

    @Query("SELECT us FROM UsersSchedule us WHERE us.user = :user AND FUNCTION('YEAR', us.workDate) = :year AND FUNCTION('MONTH', us.workDate) = :month AND us.isActive = true")
    List<UsersSchedule> findByUserAndYearMonthAndIsActiveTrue(@Param("user") User user, @Param("year") int year, @Param("month") int month);

    @Query("SELECT us FROM UsersSchedule us WHERE FUNCTION('YEAR', us.workDate) = :year AND FUNCTION('MONTH', us.workDate) = :month AND us.isActive = true")
    List<UsersSchedule> findByYearMonthAndIsActiveTrue(@Param("year") int year, @Param("month") int month);

//    Optional<UsersSchedule> findByUserAndWorkDateAndIsActiveTrue(User user, LocalDate workDate);
    boolean existsByUserAndWorkDateAndIsActiveTrue(User user, LocalDate workDate);


    List<UsersSchedule> findByWorkDateAndIsActive(LocalDate workDate, Boolean isActive);
    @Query("SELECT us FROM UsersSchedule us WHERE " +
            "(:userId IS NULL OR us.user.id = :userId) AND " +
            "(:startDate IS NULL OR us.workDate >= :startDate) AND " +
            "(:endDate IS NULL OR us.workDate <= :endDate) AND " +
            "(:month IS NULL OR MONTH(us.workDate) = :month) AND " +
            "(:year IS NULL OR YEAR(us.workDate) = :year) AND " +
            "(:status IS NULL OR us.status = :status) AND " +
            "us.isActive = true")
    List<UsersSchedule> findByFilters(@Param("userId") Long userId,
                                      @Param("startDate") LocalDate startDate,
                                      @Param("endDate") LocalDate endDate,
                                      @Param("month") Integer month,
                                      @Param("year") Integer year,
                                      @Param("status") String status);

    // Count staff with schedules on a specific date
    @Query("SELECT COUNT(DISTINCT us.user.id) FROM UsersSchedule us WHERE " +
            "us.workDate = :date AND " +
            "us.isActive = true AND " +
            "us.status IN ('pending', 'confirmed')")
    int countStaffWithScheduleOnDate(@Param("date") LocalDate date);

    // Count staff with schedules on a specific date and timeslot (if needed)
    @Query("SELECT COUNT(DISTINCT us.user.id) FROM UsersSchedule us WHERE " +
            "us.workDate = :date AND " +
            "us.isActive = true AND " +
            "us.status IN ('pending', 'confirmed') AND " +
            "(:timeSlotId IS NULL OR us.timeSlot.slotId = :timeSlotId)")
    int countStaffWithScheduleOnDateAndTimeSlot(@Param("date") LocalDate date,
                                                @Param("timeSlotId") Long timeSlotId);

}