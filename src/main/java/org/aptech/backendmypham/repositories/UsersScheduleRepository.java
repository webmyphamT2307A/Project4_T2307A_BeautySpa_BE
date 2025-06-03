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


    @Query("SELECT us FROM UsersSchedule us WHERE us.user = :user AND FUNCTION('YEAR', us.workDate) = :year AND FUNCTION('MONTH', us.workDate) = :month AND us.isActive = true")
    List<UsersSchedule> findByUserAndYearMonthAndIsActiveTrue(@Param("user") User user, @Param("year") int year, @Param("month") int month);

    @Query("SELECT us FROM UsersSchedule us WHERE FUNCTION('YEAR', us.workDate) = :year AND FUNCTION('MONTH', us.workDate) = :month AND us.isActive = true")
    List<UsersSchedule> findByYearMonthAndIsActiveTrue(@Param("year") int year, @Param("month") int month);
    Optional<UsersSchedule> findByUserAndWorkDateAndIsActiveTrue(User user, LocalDate workDate);

}