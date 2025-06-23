package org.aptech.backendmypham.repositories;

import org.aptech.backendmypham.models.Appointment;
import org.aptech.backendmypham.models.Role;
import org.aptech.backendmypham.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    Optional<Appointment> findByIdAndIsActive(Long Aid, Boolean isActive);


    int countByAppointmentDateBetweenAndServiceIdAndTimeSlot_SlotId(
            Instant start, Instant end, Long serviceId, Long slotId
    );

    @Query("SELECT a FROM Appointment a WHERE a.user.id = :userId AND a.isActive = true")
    List<Appointment> findAllByUserIdAndIsActive(Long userId);

    @Query("SELECT count(a) FROM Appointment a WHERE a.status IN ('pending', 'confirmed') AND a.appointmentDate >= :startOfDay AND a.appointmentDate <= :endOfDay AND a.isActive = true")
    long countWaitingCustomers(@Param("startOfDay") Instant startOfDay, @Param("endOfDay") Instant endOfDay);

    @Query("SELECT count(a) FROM Appointment a WHERE a.status = 'completed' AND a.appointmentDate >= :startOfDay AND a.appointmentDate <= :endOfDay AND a.isActive = true")
    long countServedCustomersToday(@Param("startOfDay") Instant startOfDay, @Param("endOfDay") Instant endOfDay);

    @Query("SELECT COALESCE(SUM(a.price), 0) FROM Appointment a WHERE a.status = 'completed' AND a.appointmentDate >= :startOfDay AND a.appointmentDate <= :endOfDay AND a.isActive = true")
    BigDecimal sumTodayRevenue(@Param("startOfDay") Instant startOfDay, @Param("endOfDay") Instant endOfDay);

    @Query("SELECT count(a) FROM Appointment a WHERE a.status = 'completed' AND a.appointmentDate >= :startOfMonth AND a.appointmentDate <= :endOfMonth AND a.isActive = true")
    long countServicesPerformedThisMonth(@Param("startOfMonth") Instant startOfMonth, @Param("endOfMonth") Instant endOfMonth);

    @Query("SELECT FUNCTION('MONTH', a.appointmentDate) as month, SUM(a.price) as revenue " +
            "FROM Appointment a " +
            "WHERE a.status = 'completed' AND FUNCTION('YEAR', a.appointmentDate) = :year AND a.isActive = true " +
            "GROUP BY FUNCTION('MONTH', a.appointmentDate)")
    List<Object[]> getMonthlyRevenue(@Param("year") int year);

    @Query("SELECT FUNCTION('YEAR', a.appointmentDate) as year, SUM(a.price) as revenue " +
            "FROM Appointment a " +
            "WHERE a.status = 'completed' AND a.isActive = true " +
            "GROUP BY FUNCTION('YEAR', a.appointmentDate)")
    List<Object[]> getYearlyRevenue();

    @Query("SELECT FUNCTION('MONTH', a.appointmentDate) as month, COUNT(DISTINCT a.customer.id) as customerCount " +
            "FROM Appointment a " +
            "WHERE a.status = 'completed' AND a.customer IS NOT NULL AND FUNCTION('YEAR', a.appointmentDate) = :year AND a.isActive = true " +
            "GROUP BY FUNCTION('MONTH', a.appointmentDate)")
    List<Object[]> getMonthlyCustomerCount(@Param("year") int year);

    @Query("SELECT FUNCTION('YEAR', a.appointmentDate) as year, COUNT(DISTINCT a.customer.id) as customerCount " +
            "FROM Appointment a " +
            "WHERE a.status = 'completed' AND a.customer IS NOT NULL AND a.isActive = true " +
            "GROUP BY FUNCTION('YEAR', a.appointmentDate) ")
    List<Object[]> getYearlyCustomerCount();

    @Query("SELECT count(a) FROM Appointment a WHERE a.user.id = :userId AND a.status IN ('pending', 'confirmed') AND a.appointmentDate >= :startOfDay AND a.appointmentDate <= :endOfDay AND a.isActive = true")
    long countWaitingCustomersForUser(@Param("userId") Long userId, @Param("startOfDay") Instant startOfDay, @Param("endOfDay") Instant endOfDay);

    @Query("SELECT COALESCE(SUM(a.price), 0) FROM Appointment a WHERE a.user.id = :userId AND a.status = 'completed' AND a.appointmentDate >= :startOfDay AND a.appointmentDate <= :endOfDay AND a.isActive = true")
    BigDecimal sumTodayRevenueForUser(@Param("userId") Long userId, @Param("startOfDay") Instant startOfDay, @Param("endOfDay") Instant endOfDay);

    @Query("SELECT FUNCTION('MONTH', a.appointmentDate) as month, SUM(a.price) as revenue " +
            "FROM Appointment a " +
            "WHERE a.user.id = :userId AND a.status = 'completed' AND FUNCTION('YEAR', a.appointmentDate) = :year AND a.isActive = true " +
            "GROUP BY FUNCTION('MONTH', a.appointmentDate)")
    List<Object[]> getMonthlyRevenueForUser(@Param("year") int year, @Param("userId") Long userId);

    @Query("SELECT count(a) FROM Appointment a WHERE a.user.id = :userId AND a.status = 'completed' AND a.appointmentDate >= :startOfDay AND a.appointmentDate <= :endOfDay AND a.isActive = true")
    long countServedCustomersTodayForUser(@Param("userId") Long userId, @Param("startOfDay") Instant startOfDay, @Param("endOfDay") Instant endOfDay);

    @Query("SELECT FUNCTION('MONTH', a.appointmentDate) as month, COUNT(DISTINCT a.customer.id) as customerCount " +
            "FROM Appointment a " +
            "WHERE a.user.id = :userId AND a.status = 'completed' AND a.customer IS NOT NULL AND FUNCTION('YEAR', a.appointmentDate) = :year AND a.isActive = true " +
            "GROUP BY FUNCTION('MONTH', a.appointmentDate)")
    List<Object[]> getMonthlyCustomerCountForUser(@Param("year") int year, @Param("userId") Long userId);

    // Đếm số dịch vụ đã thực hiện của một nhân viên trong tháng
    @Query("SELECT count(a) FROM Appointment a WHERE a.user.id = :userId AND a.status = 'completed' AND a.appointmentDate >= :startOfMonth AND a.appointmentDate <= :endOfMonth AND a.isActive = true")
    long countServicesPerformedThisMonthForUser(@Param("userId") Long userId, @Param("startOfMonth") Instant startOfMonth, @Param("endOfMonth") Instant endOfMonth);

    @Query("SELECT " +
            "FUNCTION('DAY', a.appointmentDate) as day, " +
            "CASE " +
            "  WHEN FUNCTION('HOUR', t.startTime) >= 8 AND FUNCTION('HOUR', t.startTime) < 12 THEN 'Morning' " +
            "  WHEN FUNCTION('HOUR', t.startTime) >= 12 AND FUNCTION('HOUR', t.startTime) < 18 THEN 'Afternoon' " +
            "  ELSE 'Evening' " +
            "END as shiftName, " +
            "COUNT(a.id) as appointmentCount " +
            "FROM Appointment a JOIN a.timeSlot t " + // JOIN với TimeSlots để lấy startTime
            "WHERE FUNCTION('YEAR', a.appointmentDate) = :year " +
            "AND FUNCTION('MONTH', a.appointmentDate) = :month " +
            "AND a.isActive = true " +
            "GROUP BY day, shiftName ")
    List<Object[]> getDailyCustomerCountByShift(@Param("year") int year, @Param("month") int month);
    List<Appointment> findByAppointmentDate(Instant date);

    List<Appointment> findByAppointmentDateBetween(Instant startOfDay, Instant endOfDay);

    List<Appointment> findByAppointmentDateBetweenAndUserAndIsActiveTrue(Instant startOfDay, Instant endOfDay, User user);
    List<Appointment> findByAppointmentDateBetweenAndIsActiveTrue(Instant startOfDay, Instant endOfDay);

    @Query("SELECT SUM(a.price) FROM Appointment a WHERE a.user.id = :userId AND a.appointmentDate BETWEEN :startDate AND :endDate AND a.status = 'completed'")
    Long sumCommissionByUserAndDateRange(Long userId, LocalDate startDate, LocalDate endDate);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.user.id = :userId AND a.appointmentDate BETWEEN :startDate AND :endDate AND a.status = 'completed'")
    Integer countCompletedOrdersByUserAndDateRange(Long userId, LocalDate startDate, LocalDate endDate);
    @Query("SELECT a FROM Appointment a " +
            "LEFT JOIN FETCH a.customer c " +
            "LEFT JOIN FETCH a.service s " +
            "LEFT JOIN FETCH a.user u " +
            "LEFT JOIN FETCH a.timeSlot t " +
            "WHERE c.id = :customerId AND (:isActive IS NULL OR a.isActive = :isActive) " +
            "ORDER BY a.createdAt DESC")
    Page<Appointment> findByCustomerIdWithDetailsOrderByCreatedAtDesc(
            @Param("customerId") Long customerId,
            @Param("isActive") Boolean isActive,
            Pageable pageable);

    @Query("SELECT a FROM Appointment a " +
            "LEFT JOIN FETCH a.customer c " +
            "LEFT JOIN FETCH a.service s " +
            "LEFT JOIN FETCH a.user u " +
            "LEFT JOIN FETCH a.timeSlot t " +
            "WHERE a.phoneNumber = :phoneNumber AND (:isActive IS NULL OR a.isActive = :isActive) " +
            "ORDER BY a.createdAt DESC")
    List<Appointment> findByPhoneNumberWithDetailsOrderByCreatedAtDesc(
            @Param("phoneNumber") String phoneNumber,
            @Param("isActive") Boolean isActive);

    List<Appointment> findByCustomerIdAndIsActive(Long customerId, Boolean isActive);

    @Query("SELECT a FROM Appointment a WHERE a.user.id = :userId " +
            "AND (:year IS NULL OR YEAR(a.appointmentDate) = :year) " +
            "AND (:month IS NULL OR MONTH(a.appointmentDate) = :month)")
    List<Appointment> findAppointmentsByUserIdAndDate(@Param("userId") Long userId,
                                                      @Param("year") Integer year,
                                                      @Param("month") Integer month);
    @Query("SELECT a FROM Appointment a " +
            "LEFT JOIN FETCH a.customer c " +
            "LEFT JOIN FETCH a.service s " +
            "LEFT JOIN FETCH a.user u " +
            "LEFT JOIN FETCH a.timeSlot t " +
            "WHERE c.id = :customerId AND (:isActive IS NULL OR a.isActive = :isActive) " +
            "ORDER BY a.createdAt DESC")
    Page<Appointment> findByCustomerIdWithDetailsOrderByCreatedAtDesc(
            @Param("customerId") Long customerId,
            @Param("isActive") Boolean isActive,
            Pageable pageable);

    @Query("SELECT a FROM Appointment a " +
            "LEFT JOIN FETCH a.customer c " +
            "LEFT JOIN FETCH a.service s " +
            "LEFT JOIN FETCH a.user u " +
            "LEFT JOIN FETCH a.timeSlot t " +
            "WHERE a.phoneNumber = :phoneNumber AND (:isActive IS NULL OR a.isActive = :isActive) " +
            "ORDER BY a.createdAt DESC")
    List<Appointment> findByPhoneNumberWithDetailsOrderByCreatedAtDesc(
            @Param("phoneNumber") String phoneNumber,
            @Param("isActive") Boolean isActive);

    List<Appointment> findByCustomerIdAndIsActive(Long customerId, Boolean isActive);
}
