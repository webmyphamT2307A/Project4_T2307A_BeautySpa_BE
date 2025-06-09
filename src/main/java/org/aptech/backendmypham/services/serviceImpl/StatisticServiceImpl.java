package org.aptech.backendmypham.services.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.aptech.backendmypham.dto.*;
import org.aptech.backendmypham.models.User;
import org.aptech.backendmypham.repositories.AppointmentRepository;
import org.aptech.backendmypham.repositories.ReviewRepository;
import org.aptech.backendmypham.repositories.UserRepository;
import org.aptech.backendmypham.services.StatisticService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class StatisticServiceImpl implements StatisticService {
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;

    private final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    @Override
    public DashboardSummaryDto getDashboardSummary(Long userId) {
        LocalDate today = LocalDate.now(VIETNAM_ZONE);
        Instant startOfDay = today.atStartOfDay(VIETNAM_ZONE).toInstant();
        Instant endOfDay = today.atTime(LocalTime.MAX).atZone(VIETNAM_ZONE).toInstant();

        YearMonth currentMonth = YearMonth.now(VIETNAM_ZONE);
        Instant startOfMonth = currentMonth.atDay(1).atStartOfDay(VIETNAM_ZONE).toInstant();
        Instant endOfMonth = currentMonth.atEndOfMonth().atTime(LocalTime.MAX).atZone(VIETNAM_ZONE).toInstant();

        long waiting, served, monthlyServices;
        BigDecimal revenue;
        double avgRating;

        if (userId != null) {
            // === LOGIC CHO DASHBOARD NHÂN VIÊN ===
            waiting = appointmentRepository.countWaitingCustomersForUser(userId, startOfDay, endOfDay);
            served = appointmentRepository.countServedCustomersTodayForUser(userId, startOfDay, endOfDay);
            revenue = appointmentRepository.sumTodayRevenueForUser(userId, startOfDay, endOfDay);
            monthlyServices = appointmentRepository.countServicesPerformedThisMonthForUser(userId, startOfMonth, endOfMonth);

            // Lấy rating của chính nhân viên đó
            avgRating = userRepository.findById(userId)
                    .map(User::getAverageRating)
                    .orElse(0.0);
        } else {
            // === LOGIC CHO DASHBOARD ADMIN (như cũ) ===
            waiting = appointmentRepository.countWaitingCustomers(startOfDay, endOfDay);
            served = appointmentRepository.countServedCustomersToday(startOfDay, endOfDay);
            revenue = appointmentRepository.sumTodayRevenue(startOfDay, endOfDay);
            monthlyServices = appointmentRepository.countServicesPerformedThisMonth(startOfMonth, endOfMonth);
            avgRating = userRepository.getOverallAverageRating();
        }

        return new DashboardSummaryDto(waiting, served, revenue, monthlyServices, avgRating);
    }
    @Override
    public List<ChartDataDto> getRevenueByMonth(int year, Long userId) {
        List<Object[]> results;

        if (userId != null) {
            // Lấy doanh thu cho một nhân viên cụ thể
            results = appointmentRepository.getMonthlyRevenueForUser(year, userId);
        } else {
            // Lấy doanh thu cho toàn công ty (Admin)
            results = appointmentRepository.getMonthlyRevenue(year);
        }

        Map<Integer, BigDecimal> revenueMap = results.stream()
                .collect(Collectors.toMap(
                        res -> (Integer) res[0],
                        res -> (BigDecimal) res[1]
                ));

        // Tạo đủ 12 tháng, tháng nào không có doanh thu thì mặc định là 0
        return IntStream.rangeClosed(1, 12).mapToObj(month -> {
            String monthLabel = Month.of(month).name().substring(0, 3);
            BigDecimal value = revenueMap.getOrDefault(month, BigDecimal.ZERO);
            return new ChartDataDto(monthLabel, value);
        }).collect(Collectors.toList());
    }

    @Override
    public List<ChartDataDto> getRevenueByYear() {
        List<Object[]> results = appointmentRepository.getYearlyRevenue();
        return results.stream().map(res -> new ChartDataDto(
                String.valueOf(res[0]), // year
                (BigDecimal) res[1]      // revenue
        )).collect(Collectors.toList());
    }

    @Override
    public List<RoleRatingDto> getRoleRatings() {
        List<User> users = userRepository.findAllActiveWithRoles();

        Map<String, List<User>> usersByRole = users.stream()
                .collect(Collectors.groupingBy(user -> user.getRole().getName()));

        return usersByRole.entrySet().stream().map(entry -> {
            String roleName = entry.getKey();
            List<User> roleUsers = entry.getValue();

            double avgRating = roleUsers.stream()
                    .filter(u -> u.getAverageRating() != null && u.getAverageRating() > 0)
                    .mapToDouble(User::getAverageRating)
                    .average()
                    .orElse(0.0);

            int totalReviews = roleUsers.stream()
                    .filter(u -> u.getTotalReviews() != null)
                    .mapToInt(User::getTotalReviews)
                    .sum();

            return new RoleRatingDto(roleName, avgRating, totalReviews);
        }).collect(Collectors.toList());
    }
    @Override
    public List<ChartDataDto> getCustomerCountByMonth(int year,Long userId) {

        List<Object[]> results = appointmentRepository.getMonthlyCustomerCount(year);
        if (userId != null) {
            results = appointmentRepository.getMonthlyCustomerCountForUser(year, userId);
        } else {
            results = appointmentRepository.getMonthlyCustomerCount(year);
        }
        Map<Integer, BigDecimal> customerMap = results.stream()
                .collect(Collectors.toMap(
                        res -> (Integer) res[0],
                        res -> new BigDecimal((Long) res[1])
                ));

        return IntStream.rangeClosed(1, 12).mapToObj(month -> {
            String monthLabel = Month.of(month).name().substring(0, 3);
            BigDecimal value = customerMap.getOrDefault(month, BigDecimal.ZERO);
            return new ChartDataDto(monthLabel, value);
        }).collect(Collectors.toList());
    }

    @Override
    public List<ChartDataDto> getCustomerCountByYear() {
        List<Object[]> results = appointmentRepository.getYearlyCustomerCount();
        return results.stream().map(res -> new ChartDataDto(
                String.valueOf(res[0]),      // year
                new BigDecimal((Long) res[1]) // customer count
        )).collect(Collectors.toList());
    }
    @Override
    public List<ChartDataDto> getMyMonthlyRatings(int year, Long userId) {
        // Gọi đến câu query đã tạo trong ReviewRepository
        List<Object[]> results = userRepository.getMonthlyRatingsForUser(year, userId);

        // Chuyển kết quả từ List<Object[]> thành Map<Tháng, Rating> để dễ xử lý
        Map<Integer, BigDecimal> ratingMap = results.stream()
                .collect(Collectors.toMap(
                        res -> (Integer) res[0], // Key là tháng (Integer)
                        // Value là rating (Double), chuyển sang BigDecimal để đồng nhất
                        res -> BigDecimal.valueOf((Double) res[1])
                ));

        // Tạo danh sách 12 tháng, tháng nào không có dữ liệu trong map thì rating = 0
        return IntStream.rangeClosed(1, 12).mapToObj(month -> {
            String monthLabel = Month.of(month).name().substring(0, 3); // Chuyển 1 -> "JAN", 2 -> "FEB"
            BigDecimal value = ratingMap.getOrDefault(month, BigDecimal.ZERO);
            return new ChartDataDto(monthLabel, value);
        }).collect(Collectors.toList());
    }
    @Override
    public List<DailyCustomerReportDto> getDailyCustomerReport(int year, int month) {
        List<Object[]> results = appointmentRepository.getDailyCustomerCountByShift(year, month);

        // Sử dụng Map để nhóm các ca theo từng ngày
        // Key: Ngày trong tháng (Integer), Value: Danh sách các ca trong ngày đó (List<ShiftReportDto>)
        Map<Integer, List<ShiftReportDto>> shiftsByDay = new HashMap<>();

        for (Object[] row : results) {
            Integer day = (Integer) row[0];
            String shiftName = (String) row[1];
            long count = (Long) row[2];

            // Nếu ngày chưa có trong map, tạo một list mới
            shiftsByDay.computeIfAbsent(day, k -> new ArrayList<>())
                    .add(new ShiftReportDto(shiftName, count));
        }

        // Chuyển Map đã nhóm thành List<DailyCustomerReportDto> theo đúng định dạng FE cần
        return shiftsByDay.entrySet().stream()
                .map(entry -> {
                    Integer day = entry.getKey();
                    List<ShiftReportDto> shifts = entry.getValue();

                    // Tính tổng số khách trong ngày từ các ca
                    long totalCount = shifts.stream().mapToLong(ShiftReportDto::getCount).sum();

                    // Định dạng ngày thành chuỗi "yyyy-MM-dd"
                    String dateStr = String.format("%d-%02d-%02d", year, month, day);

                    return new DailyCustomerReportDto(dateStr, totalCount, shifts);
                })
                .sorted(Comparator.comparing(DailyCustomerReportDto::getDate)) // Sắp xếp kết quả theo ngày
                .collect(Collectors.toList());
    }


}
