package org.aptech.backendmypham.services.serviceImpl;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.aptech.backendmypham.dto.ChartDataDto;
import org.aptech.backendmypham.dto.DashboardSummaryDto;
import org.aptech.backendmypham.dto.RoleRatingDto;
import org.aptech.backendmypham.models.User;
import org.aptech.backendmypham.repositories.AppointmentRepository;
import org.aptech.backendmypham.repositories.UserRepository;
import org.aptech.backendmypham.services.StatisticService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class StatisticServiceImpl implements StatisticService {
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    @Override
    public DashboardSummaryDto getDashboardSummary() {
        LocalDate today = LocalDate.now(VIETNAM_ZONE);
        Instant startOfDay = today.atStartOfDay(VIETNAM_ZONE).toInstant();
        Instant endOfDay = today.atTime(LocalTime.MAX).atZone(VIETNAM_ZONE).toInstant();

        YearMonth currentMonth = YearMonth.now(VIETNAM_ZONE);
        Instant startOfMonth = currentMonth.atDay(1).atStartOfDay(VIETNAM_ZONE).toInstant();
        Instant endOfMonth = currentMonth.atEndOfMonth().atTime(LocalTime.MAX).atZone(VIETNAM_ZONE).toInstant();

        long waiting = appointmentRepository.countWaitingCustomers(startOfDay, endOfDay);
        long served = appointmentRepository.countServedCustomersToday(startOfDay, endOfDay);
        BigDecimal revenue = appointmentRepository.sumTodayRevenue(startOfDay, endOfDay);
        long monthlyServices = appointmentRepository.countServicesPerformedThisMonth(startOfMonth, endOfMonth);
        double avgRating = userRepository.getOverallAverageRating();

        return new DashboardSummaryDto(waiting, served, revenue, monthlyServices, avgRating);
    }

    @Override
    public List<ChartDataDto> getRevenueByMonth(int year) {
        List<Object[]> results = appointmentRepository.getMonthlyRevenue(year);
        Map<Integer, BigDecimal> revenueMap = results.stream()
                .collect(Collectors.toMap(
                        res -> (Integer) res[0],
                        res -> (BigDecimal) res[1]
                ));

        // Tạo đủ 12 tháng, tháng nào không có doanh thu thì mặc định là 0
        return IntStream.rangeClosed(1, 12).mapToObj(month -> {
            String monthLabel = Month.of(month).name().substring(0, 3); // JAN, FEB,...
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
    public List<ChartDataDto> getCustomerCountByMonth(int year) {
        List<Object[]> results = appointmentRepository.getMonthlyCustomerCount(year);
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

}
