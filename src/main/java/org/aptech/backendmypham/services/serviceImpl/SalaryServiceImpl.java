package org.aptech.backendmypham.services.serviceImpl;

import org.aptech.backendmypham.dto.SalaryDetails;
import org.aptech.backendmypham.models.*;
import org.aptech.backendmypham.repositories.*;
import org.springframework.transaction.annotation.Transactional;
import lombok.AllArgsConstructor;
import org.aptech.backendmypham.dto.CalculateSalaryRequestDto;
import org.aptech.backendmypham.dto.SalaryRequestDto;
import org.aptech.backendmypham.dto.SalaryResponseDto;
import org.aptech.backendmypham.exception.ResourceNotFoundException;
import org.aptech.backendmypham.services.SalaryService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
// import java.time.temporal.ChronoUnit; // Không còn dùng ChronoUnit nếu không tính giờ làm việc chi tiết
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class SalaryServiceImpl implements SalaryService {
    private final SalaryRepository salaryRepository;
    private final UserRepository userRepository;
    private final UsersScheduleRepository usersScheduleRepository;
    private final AppointmentRepository appointmentRepository;
    private final AttendanceRepository attendanceRepository;
    private  final BookingRepository bookingRepository;

    // Các hằng số cho giảm trừ thuế TNCN
    // TODO: Cập nhật các giá trị này theo quy định mới nhất của Việt Nam
    // và cân nhắc đưa ra file cấu hình hoặc bảng DB.
    private static final BigDecimal PERSONAL_DEDUCTION_AMOUNT = new BigDecimal("11000000");
    private static final BigDecimal DEPENDENT_DEDUCTION_AMOUNT = new BigDecimal("4400000");

    // Các hằng số bảo hiểm đã được bỏ qua

    @Override
    @Transactional
    public SalaryResponseDto calculateAndSaveSalary(CalculateSalaryRequestDto calculateDto) {
        User user = userRepository.findById(calculateDto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy User với ID: " + calculateDto.getUserId()));

        BigDecimal standardFixedSalary = user.getStandardBaseSalary();
        if (standardFixedSalary == null || standardFixedSalary.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Lương cứng chuẩn của nhân viên " + user.getFullName() + " chưa được thiết lập hoặc không hợp lệ.");
        }
        Integer numberOfDependents = user.getNumberOfDependents() != null ? user.getNumberOfDependents() : 0;

        YearMonth salaryMonthYear = YearMonth.of(calculateDto.getYear(), calculateDto.getMonth());
        LocalDate startDateOfMonth = salaryMonthYear.atDay(1);
        LocalDate endDateOfMonth = salaryMonthYear.atEndOfMonth();

        List<UsersSchedule> userSchedulesInMonth = usersScheduleRepository
                .findByUserAndWorkDateBetweenAndIsActiveTrue(user, startDateOfMonth, endDateOfMonth);

        long standardWorkingDaysInMonth = calculateStandardWorkingDays(salaryMonthYear);
        double actualPaidWorkingDays = 0;

        for (UsersSchedule schedule : userSchedulesInMonth) {
            if (schedule.getStatus() != null) {
                String status = schedule.getStatus().toLowerCase();
                switch (status) {
                    case "completed":
                    case "paid_leave":
                    case "confirmed":
                    case "holiday_paid":
                        actualPaidWorkingDays += 1.0;
                        break;
                    case "half_day_paid_leave":
                    case "half_day_work":
                        actualPaidWorkingDays += 0.5;
                        break;
                    default:
                        break;
                }
            }
        }

        BigDecimal actualFixedSalary = standardFixedSalary;
        if (standardWorkingDaysInMonth > 0 && actualPaidWorkingDays < standardWorkingDaysInMonth) {
            actualFixedSalary = standardFixedSalary
                    .multiply(BigDecimal.valueOf(actualPaidWorkingDays))
                    .divide(BigDecimal.valueOf(standardWorkingDaysInMonth), 0, RoundingMode.HALF_UP);
        } else if (actualPaidWorkingDays == 0 && standardWorkingDaysInMonth > 0) {
            actualFixedSalary = BigDecimal.ZERO;
        }

        BigDecimal commissionAmount = calculateDto.getManualBonus() != null ? calculateDto.getManualBonus() : BigDecimal.ZERO;
        commissionAmount = commissionAmount.setScale(0, RoundingMode.HALF_UP);

        BigDecimal grossIncome = actualFixedSalary.add(commissionAmount);

        // --- PHẦN TÍNH BẢO HIỂM ĐÃ BỎ QUA ---
        BigDecimal totalInsuranceDeduction = BigDecimal.ZERO; // Không có khấu trừ bảo hiểm

        // --- TÍNH THUẾ THU NHẬP CÁ NHÂN (PIT) ---
        // Thu nhập tính thuế sẽ không trừ bảo hiểm nữa
        BigDecimal personalDeduction = PERSONAL_DEDUCTION_AMOUNT;
        BigDecimal dependentDeductionTotal = DEPENDENT_DEDUCTION_AMOUNT.multiply(BigDecimal.valueOf(numberOfDependents));

        // Thu nhập tính thuế = Tổng thu nhập - Giảm trừ bản thân - Giảm trừ người phụ thuộc
        // (Không còn trừ totalInsuranceDeduction ở đây vì đã bỏ qua bảo hiểm)
        BigDecimal taxableIncomeForPIT = grossIncome
                .subtract(personalDeduction)
                .subtract(dependentDeductionTotal);

        BigDecimal pitAmount = BigDecimal.ZERO;
        if (taxableIncomeForPIT.compareTo(BigDecimal.ZERO) > 0) {
            pitAmount = calculateProgressivePIT(taxableIncomeForPIT);
        }
        pitAmount = pitAmount.setScale(0, RoundingMode.HALF_UP);

        BigDecimal otherDeductions = calculateDto.getManualDeductions() != null ? calculateDto.getManualDeductions() : BigDecimal.ZERO;
        otherDeductions = otherDeductions.setScale(0, RoundingMode.HALF_UP);

        // Tổng khấu trừ giờ chỉ bao gồm PIT và các khấu trừ khác
        BigDecimal totalDeductions = pitAmount.add(otherDeductions);

        BigDecimal netSalary = grossIncome.subtract(totalDeductions);
        netSalary = netSalary.setScale(0, RoundingMode.HALF_UP);

        Salary salaryRecord = salaryRepository.findByUserAndMonthAndYear(user, calculateDto.getMonth(), calculateDto.getYear())
                .orElseGet(() -> {
                    Salary newSalary = new Salary();
                    newSalary.setUser(user);
                    newSalary.setMonth(calculateDto.getMonth());
                    newSalary.setYear(calculateDto.getYear());
                    newSalary.setCreatedAt(java.time.Instant.now());
                    newSalary.setIsActive(true);
                    return newSalary;
                });

        salaryRecord.setBaseSalary(actualFixedSalary);
        salaryRecord.setBonus(commissionAmount);
        salaryRecord.setDeductions(totalDeductions);
        salaryRecord.setTotalSalary(netSalary);
        salaryRecord.setPaymentDate(salaryMonthYear.atEndOfMonth().plusDays(5));
        salaryRecord.setNotes(calculateDto.getNotesForCalculation());
        if (salaryRecord.getId() == null) {
            salaryRecord.setIsActive(true);
        }

        Salary savedSalary = salaryRepository.save(salaryRecord);
        return mapToResponseDto(savedSalary);
    }

    private long calculateStandardWorkingDays(YearMonth yearMonth) {
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();
        long workingDays = 0;
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            DayOfWeek day = date.getDayOfWeek();
            if (!(day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY)) {
                workingDays++;
            }
        }
        return workingDays;
    }

    // TODO: BẮT BUỘC CẬP NHẬT BIỂU THUẾ NÀY THEO QUY ĐỊNH MỚI NHẤT CỦA VIỆT NAM
    private BigDecimal calculateProgressivePIT(BigDecimal taxableIncomePerMonth) {
        BigDecimal tax = BigDecimal.ZERO;
        if (taxableIncomePerMonth.compareTo(new BigDecimal("5000000")) <= 0) {
            tax = taxableIncomePerMonth.multiply(new BigDecimal("0.05"));
        } else if (taxableIncomePerMonth.compareTo(new BigDecimal("10000000")) <= 0) {
            tax = new BigDecimal("250000").add(
                    (taxableIncomePerMonth.subtract(new BigDecimal("5000000"))).multiply(new BigDecimal("0.10"))
            );
        } else if (taxableIncomePerMonth.compareTo(new BigDecimal("18000000")) <= 0) {
            tax = new BigDecimal("750000").add(
                    (taxableIncomePerMonth.subtract(new BigDecimal("10000000"))).multiply(new BigDecimal("0.15"))
            );
        } else if (taxableIncomePerMonth.compareTo(new BigDecimal("32000000")) <= 0) {
            tax = new BigDecimal("1950000").add(
                    (taxableIncomePerMonth.subtract(new BigDecimal("18000000"))).multiply(new BigDecimal("0.20"))
            );
        } else if (taxableIncomePerMonth.compareTo(new BigDecimal("52000000")) <= 0) {
            tax = new BigDecimal("4750000").add(
                    (taxableIncomePerMonth.subtract(new BigDecimal("32000000"))).multiply(new BigDecimal("0.25"))
            );
        } else if (taxableIncomePerMonth.compareTo(new BigDecimal("80000000")) <= 0) {
            tax = new BigDecimal("9750000").add(
                    (taxableIncomePerMonth.subtract(new BigDecimal("52000000"))).multiply(new BigDecimal("0.30"))
            );
        } else { // Trên 80 triệu
            tax = new BigDecimal("18150000").add(
                    (taxableIncomePerMonth.subtract(new BigDecimal("80000000"))).multiply(new BigDecimal("0.35"))
            );
        }
        return tax.max(BigDecimal.ZERO);
    }

    private SalaryResponseDto mapToResponseDto(Salary entity) {
        SalaryResponseDto dto = new SalaryResponseDto();
        dto.setId(entity.getId());
        if (entity.getUser() != null) {
            dto.setUserId(Long.valueOf(entity.getUser().getId()));
            if (entity.getUser().getFullName() != null && !entity.getUser().getFullName().isEmpty()) {
                dto.setUserName(entity.getUser().getFullName());
            } else if (entity.getUser().getFullName() != null) {
                dto.setUserName(entity.getUser().getFullName());
            } else {
                dto.setUserName("N/A");
            }
            dto.setUserEmail(entity.getUser().getEmail());
        }
        dto.setMonth(entity.getMonth());
        dto.setYear(entity.getYear());
        dto.setBaseSalary(entity.getBaseSalary());
        dto.setBonus(entity.getBonus());
        dto.setDeductions(entity.getDeductions());
        dto.setTotalSalary(entity.getTotalSalary());
        dto.setPaymentDate(entity.getPaymentDate());
        dto.setNotes(entity.getNotes());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setIsActive(entity.getIsActive());
        return dto;
    }

    @Override
    public SalaryResponseDto getSalaryById(Integer salaryId) {
        Salary salary = salaryRepository.findById(salaryId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Salary với ID: " + salaryId));
        return mapToResponseDto(salary);
    }

    @Override
    public List<SalaryResponseDto> findSalaries(Long userId, Integer month, Integer year) {
        List<Salary> salaries;
        if (userId != null && month != null && year != null) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy User với ID: " + userId));
            salaries = salaryRepository.findByUserAndMonthAndYearAndIsActiveTrue(user, month, year);
        } else if (userId != null) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy User với ID: " + userId));
            salaries = salaryRepository.findByUserAndIsActiveTrue(user);
        } else if (month != null && year != null) {
            salaries = salaryRepository.findByMonthAndYearAndIsActiveTrue(month, year);
        } else {
            salaries = salaryRepository.findByIsActiveTrue();
        }
        return salaries.stream().map(this::mapToResponseDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SalaryResponseDto updateSalary(Integer salaryId, SalaryRequestDto requestDto) {
        Salary existingSalary = salaryRepository.findById(salaryId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Salary với ID: " + salaryId + " để cập nhật"));

        if (requestDto.getUserId() != null && !existingSalary.getUser().getId().equals(requestDto.getUserId())) {
            throw new IllegalArgumentException("Không thể thay đổi nhân viên của bản ghi lương.");
        }
        // Cho phép cập nhật các trường khác, nhưng user, month, year thì không đổi
        // Nếu muốn thay đổi kỳ lương hoặc nhân viên, nên tạo bản ghi mới.

        if(requestDto.getBaseSalary() != null) existingSalary.setBaseSalary(requestDto.getBaseSalary());
        existingSalary.setBonus(requestDto.getBonus() != null ? requestDto.getBonus() : existingSalary.getBonus());
        existingSalary.setDeductions(requestDto.getDeductions() != null ? requestDto.getDeductions() : existingSalary.getDeductions());
        if(requestDto.getTotalSalary() != null) {
            BigDecimal totalSalary = existingSalary.getBaseSalary().add(existingSalary.getBonus()).subtract(existingSalary.getDeductions());
            existingSalary.setTotalSalary(totalSalary);
        }
        if(requestDto.getPaymentDate() != null) existingSalary.setPaymentDate(requestDto.getPaymentDate());
        if(requestDto.getNotes() != null) existingSalary.setNotes(requestDto.getNotes());
        if (requestDto.getIsActive() != null) {
            existingSalary.setIsActive(requestDto.getIsActive());
        }
        Salary updatedSalary = salaryRepository.save(existingSalary);
        return mapToResponseDto(updatedSalary);
    }

    @Override
    @Transactional
    public boolean deleteSalary(Integer salaryId) {
        Salary salaryToDelete = salaryRepository.findById(salaryId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Salary với ID: " + salaryId + " để xóa"));
        salaryToDelete.setIsActive(false);
        salaryRepository.save(salaryToDelete);
        return true;
    }

    @Override
    public SalaryDetails getEstimatedSalary(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        // Fetch base salary
        Long baseSalary = user.getStandardBaseSalary().longValue();

        // Calculate worked days and total workdays
        int workedDays = Math.toIntExact(attendanceRepository.countWorkedDays(userId, LocalDate.now().getMonthValue()));
        int totalWorkdays = Math.toIntExact(attendanceRepository.getTotalWorkdays(userId, LocalDate.now().getMonthValue()));

        // Calculate total hours
        long totalHours = Optional.ofNullable(attendanceRepository.sumTotalHours(userId, LocalDate.now().getMonthValue()))
                .orElse(0L);
        // Fetch bookings for the current month
        List<Appointment> appointments = appointmentRepository.findAppointmentsByUserIdAndDate(userId, LocalDate.now().getYear(), LocalDate.now().getMonthValue());

        // Calculate total tip
        double moneyTip = 0L;
        for (Appointment appointment : appointments) {
            System.out.println("Appointment ID: " + appointment.getId() + ", Status: " + appointment.getStatus());
            if (appointment.getPrice() != null && "completed".equals(appointment.getStatus())) {
                moneyTip += appointment.getPrice().longValue() * 0.1; // Assume tip is 10% of total booking price
            }
        }
        double totalTip = moneyTip;

        // Calculate total salary
        double totalSalary = baseSalary + totalTip;

        return new SalaryDetails(
                baseSalary,
                workedDays,
                totalWorkdays,
                totalHours,
                totalTip,
                totalSalary
        );
    }
}
