package org.aptech.backendmypham.services.serviceImpl;

// Đảm bảo import đúng Transactional của Spring
import org.aptech.backendmypham.dto.ScheduleUserDto;
import org.springframework.transaction.annotation.Transactional;
import lombok.AllArgsConstructor;
import org.aptech.backendmypham.dto.UsersScheduleRequestDto;
import org.aptech.backendmypham.dto.UsersScheduleResponseDto; // Import DTO Response
import org.aptech.backendmypham.exception.ResourceNotFoundException; // Import custom exception
import org.aptech.backendmypham.models.User;
import org.aptech.backendmypham.models.UsersSchedule;
import org.aptech.backendmypham.repositories.UserRepository;
import org.aptech.backendmypham.repositories.UsersScheduleRepository;
import org.aptech.backendmypham.services.UsersScheduleService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UsersScheduleServiceImpl implements UsersScheduleService {
    private final UsersScheduleRepository usersScheduleRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public UsersScheduleResponseDto createSchedule(UsersScheduleRequestDto requestDto) {
        // 1. Tìm user từ ID trong DTO
        User user = userRepository.findById(requestDto.getUserId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + requestDto.getUserId()));

        // 2. Lấy tất cả lịch làm việc (còn hoạt động) của user trong ngày đó để kiểm tra
        List<UsersSchedule> existingSchedules = usersScheduleRepository.findByUserAndWorkDateAndIsActiveTrue(user, requestDto.getWorkDate());

        // 3. Tạo và thiết lập đầy đủ thông tin cho đối tượng UsersSchedule mới từ DTO
        UsersSchedule newSchedule = new UsersSchedule();
        newSchedule.setUser(user);
        newSchedule.setWorkDate(requestDto.getWorkDate());
        newSchedule.setShift(requestDto.getShift());
        newSchedule.setStatus(requestDto.getStatus()); // Giả sử status có trong DTO

        // Các trường tùy chọn từ form (dựa theo code FE bạn đã gửi)
        newSchedule.setCheckInTime(requestDto.getCheckInTime());
        newSchedule.setCheckOutTime(requestDto.getCheckOutTime());

        // Thiết lập các giá trị mặc định một cách tường minh
        newSchedule.setIsActive(true);
        newSchedule.setIsLastTask(false); // Hoặc lấy từ DTO nếu có: requestDto.getIsLastTask()

        // 4. *** GỌI HÀM KIỂM TRA TRÙNG LẶP ***
        // Sử dụng đối tượng newSchedule vừa được tạo để kiểm tra
        checkForScheduleConflict(newSchedule, existingSchedules);

        // 5. Nếu không có lỗi, lưu đối tượng newSchedule vào cơ sở dữ liệu
        UsersSchedule savedSchedule = usersScheduleRepository.save(newSchedule);

        // 6. Ánh xạ entity đã lưu sang DTO để trả về cho client
        return mapToResponseDto(savedSchedule); // Giả sử bạn có hàm mapToResponseDto
    }


    @Override
    public List<UsersScheduleResponseDto> findSchedules(Long userId, LocalDate startDate, LocalDate endDate, Integer month, Integer year, String status) {
        // Logic filter phức tạp hơn (theo month, year, status) nên được triển khai
        // bằng cách sử dụng JPA Specifications, Criteria API, hoặc các phương thức query tùy chỉnh
        // trong UsersScheduleRepository.
        // Ví dụ này giữ nguyên logic filter cơ bản của bạn.

        List<UsersSchedule> schedules;
        if (userId != null && startDate != null && endDate != null) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy User với ID: " + userId));
            // Cần định nghĩa phương thức này trong UsersScheduleRepository
            schedules = usersScheduleRepository.findByUserAndWorkDateBetweenAndIsActiveTrue(user, startDate, endDate);
        } else if (userId != null) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy User với ID: " + userId));
            // Cần định nghĩa phương thức này trong UsersScheduleRepository
            schedules = usersScheduleRepository.findByUserAndIsActiveTrue(user);
        }
        else {
            // Cần định nghĩa phương thức này trong UsersScheduleRepository
            schedules = usersScheduleRepository.findByIsActiveTrue();
        }

         if (month != null && year != null) {
             schedules = schedules.stream()
                 .filter(s -> s.getWorkDate().getMonthValue() == month && s.getWorkDate().getYear() == year)
                 .collect(Collectors.toList());
         }
         if (status != null && !status.isEmpty()) {
             schedules = schedules.stream()
                 .filter(s -> status.equalsIgnoreCase(s.getStatus()))
                 .collect(Collectors.toList());
         }

        return schedules.stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public UsersScheduleResponseDto getScheduleById(Integer scheduleId) {
        // Cần định nghĩa phương thức này trong UsersScheduleRepository
        UsersSchedule usersSchedule = usersScheduleRepository.findByIdAndIsActiveTrue(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy UsersSchedule với ID: " + scheduleId + " hoặc đã bị vô hiệu hóa"));
        return mapToResponseDto(usersSchedule);
    }

    @Override
    @Transactional
    public UsersScheduleResponseDto updateSchedule(Integer scheduleId, UsersScheduleRequestDto requestDto) {
        // 1. Lấy lịch trình hiện tại từ DB bằng Long ID
        UsersSchedule scheduleToUpdate = usersScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch trình với ID: " + scheduleId));

        // 2. Lấy user từ lịch trình. Việc thay đổi user cho một lịch trình đã có thường không được khuyến khích.
        User user = scheduleToUpdate.getUser();

        // Nếu bạn cho phép thay đổi cả user, bạn sẽ cần lấy user mới từ DTO
        // User user = userRepository.findById(requestDto.getUserId()).orElseThrow(...);

        // 3. Lấy tất cả lịch trình của user trong ngày làm việc MỚI để kiểm tra
        // Nếu ngày không đổi, nó sẽ lấy các lịch trình trong ngày cũ.
        LocalDate targetWorkDate = (requestDto.getWorkDate() != null) ? requestDto.getWorkDate() : scheduleToUpdate.getWorkDate();
        List<UsersSchedule> existingSchedules = usersScheduleRepository.findByUserAndWorkDateAndIsActiveTrue(user, targetWorkDate);

        // 4. Cập nhật TẤT CẢ thông tin từ DTO vào đối tượng scheduleToUpdate
        // Việc này hợp nhất tất cả các thay đổi vào một chỗ, giúp mã dễ đọc hơn.
        if (requestDto.getWorkDate() != null) {
            scheduleToUpdate.setWorkDate(requestDto.getWorkDate());
        }
        if (requestDto.getShift() != null) {
            scheduleToUpdate.setShift(requestDto.getShift());
        }
        if (requestDto.getStatus() != null) {
            scheduleToUpdate.setStatus(requestDto.getStatus());
        }
        if (requestDto.getIsLastTask() != null) {
            scheduleToUpdate.setIsLastTask(requestDto.getIsLastTask());
        }
        if (requestDto.getIsActive() != null) {
            scheduleToUpdate.setIsActive(requestDto.getIsActive());
        }

        // Cho phép cập nhật giờ check-in/out thành null nếu DTO truyền giá trị null
        scheduleToUpdate.setCheckInTime(requestDto.getCheckInTime());
        scheduleToUpdate.setCheckOutTime(requestDto.getCheckOutTime());

        // 5. *** GỌI HÀM KIỂM TRA TRÙNG LẶP ***
        // Hàm này sẽ tự động bỏ qua việc so sánh lịch trình với chính nó qua ID,
        // nên nó hoạt động đúng ngay cả khi đối tượng đã được thay đổi trong bộ nhớ.
        checkForScheduleConflict(scheduleToUpdate, existingSchedules);

        // 6. Nếu không có lỗi, lưu đối tượng đã được cập nhật
        UsersSchedule updatedSchedule = usersScheduleRepository.save(scheduleToUpdate);

        // 7. Ánh xạ và trả về kết quả
        return mapToResponseDto(updatedSchedule);
    }

    @Override
    @Transactional
    public boolean deleteSchedule(Integer scheduleId) {
        UsersSchedule scheduleToDelete = usersScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy UsersSchedule với ID: " + scheduleId + " để xóa"));


        scheduleToDelete.setIsActive(false);
        usersScheduleRepository.save(scheduleToDelete);
        return true;
    }
    @Override
    @Transactional
    public UsersScheduleResponseDto checkIn(Integer scheduleId) {
        // Tìm lịch trình theo ID, nếu không thấy sẽ báo lỗi
        UsersSchedule schedule = usersScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lịch trình với ID: " + scheduleId));

        // --- Logic kiểm tra nghiệp vụ ---
        // 1. Kiểm tra xem đã check-in chưa
        if (schedule.getCheckInTime() != null) {
            throw new IllegalStateException("Bạn đã check-in cho ca làm việc này rồi.");
        }
        // 2. Kiểm tra có đúng ngày làm việc không (tùy chọn, nhưng nên có)
        if (!schedule.getWorkDate().equals(LocalDate.now())) {
            throw new IllegalStateException("Không thể check-in cho một ngày làm việc khác ngày hôm nay.");
        }

        // --- Cập nhật thông tin ---
        schedule.setCheckInTime(LocalTime.now()); // Lấy giờ hiện tại
        schedule.setStatus("confirmed");      // Cập nhật trạng thái

        UsersSchedule savedSchedule = usersScheduleRepository.save(schedule);
        return mapToResponseDto(savedSchedule);
    }

    @Override
    @Transactional
    public UsersScheduleResponseDto checkOut(Integer scheduleId) {
        // Tìm lịch trình theo ID
        UsersSchedule schedule = usersScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lịch trình với ID: " + scheduleId));

        // --- Logic kiểm tra nghiệp vụ ---
        // 1. Phải check-in trước khi check-out
        if (schedule.getCheckInTime() == null) {
            throw new IllegalStateException("Bạn phải check-in trước khi check-out.");
        }
        // 2. Kiểm tra xem đã check-out chưa
        if (schedule.getCheckOutTime() != null) {
            throw new IllegalStateException("Bạn đã check-out cho ca làm việc này rồi.");
        }

        // --- Cập nhật thông tin ---
        schedule.setCheckOutTime(LocalTime.now()); // Lấy giờ hiện tại
        schedule.setStatus("completed");     // Cập nhật trạng thái

        UsersSchedule savedSchedule = usersScheduleRepository.save(schedule);
        return mapToResponseDto(savedSchedule);
    }

    @Override
    public List<ScheduleUserDto> getUserScheduleByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy User với ID: " + userId));
        List<UsersSchedule> schedules = usersScheduleRepository.findByUserAndIsActiveTrue(user);
        return schedules.stream()
                .map(schedule -> {
                    ScheduleUserDto dto = new ScheduleUserDto();
                    dto.setId(schedule.getId());
                    dto.setShift(schedule.getShift());
                    dto.setDate(schedule.getWorkDate());
                    dto.setStatus(schedule.getStatus());
                    return dto;
                })
                .collect(Collectors.toList());
    }
    private void checkForScheduleConflict(UsersSchedule newSchedule, List<UsersSchedule> existingSchedules) {
        // 1. Tách thời gian bắt đầu và kết thúc của lịch mới
        LocalTime[] newTimeRange = parseShiftTimes(newSchedule.getShift());
        if (newTimeRange == null) {
            // Nếu định dạng shift không đúng, không thể kiểm tra
            // Có thể throw lỗi ở đây nếu muốn chặt chẽ hơn
            return;
        }
        LocalTime newStartTime = newTimeRange[0];
        LocalTime newEndTime = newTimeRange[1];

        for (UsersSchedule existingSchedule : existingSchedules) {
            // Bỏ qua việc so sánh một lịch trình với chính nó (quan trọng khi cập nhật)
            if (existingSchedule.getId().equals(newSchedule.getId())) {
                continue;
            }

            // 2. Tách thời gian của lịch đã có
            LocalTime[] existingTimeRange = parseShiftTimes(existingSchedule.getShift());
            if (existingTimeRange == null) {
                continue; // Bỏ qua nếu lịch đã có bị lỗi định dạng
            }
            LocalTime existingStartTime = existingTimeRange[0];
            LocalTime existingEndTime = existingTimeRange[1];

            // 3. Logic kiểm tra chồng chéo (overlap)
            // Hai khoảng thời gian (start1, end1) và (start2, end2) bị chồng chéo nếu:
            // start1 < end2 VÀ start2 < end1
            if (newStartTime.isBefore(existingEndTime) && existingStartTime.isBefore(newEndTime)) {
                // Nếu có trùng lặp, ném ra một Exception để báo lỗi
                throw new IllegalArgumentException(
                        "Lịch làm việc bị trùng lặp với một lịch đã có: " +
                                existingSchedule.getShift()
                );
            }
        }
    }

    /**
     * Helper để tách thời gian từ chuỗi shift, ví dụ: "Sáng (08:00 - 12:00)"
     * @param shift String chứa thông tin ca làm.
     * @return Một mảng LocalTime gồm 2 phần tử [startTime, endTime], hoặc null nếu lỗi.
     */
    private LocalTime[] parseShiftTimes(String shift) {
        if (shift == null || shift.isEmpty()) {
            return null;
        }
        // Sử dụng Regex để tìm các chuỗi thời gian HH:mm
        Pattern pattern = Pattern.compile("(\\d{2}:\\d{2})");
        Matcher matcher = pattern.matcher(shift);

        LocalTime[] times = new LocalTime[2];
        int count = 0;
        while (matcher.find() && count < 2) {
            times[count++] = LocalTime.parse(matcher.group(1));
        }

        if (count == 2) {
            return times;
        }
        return null; // Không tìm thấy đủ 2 mốc thời gian
    }

    // --- Helper methods cho việc mapping ---
    private UsersSchedule mapToEntity(UsersScheduleRequestDto dto) {
        UsersSchedule entity = new UsersSchedule();
        // User sẽ được set riêng sau khi lấy từ DB trong phương thức gọi
        entity.setShift(dto.getShift());
        entity.setWorkDate(dto.getWorkDate());
        entity.setCheckInTime(dto.getCheckInTime());
        entity.setCheckOutTime(dto.getCheckOutTime());
        entity.setStatus(dto.getStatus());
        // Chỉ set nếu DTO cung cấp, để giữ giá trị mặc định từ DB nếu DTO là null
        if (dto.getIsLastTask() != null) entity.setIsLastTask(dto.getIsLastTask());
        if (dto.getIsActive() != null) entity.setIsActive(dto.getIsActive());
        return entity;
    }

    // Sửa tên kiểu trả về từ UsersScheduleResponseDT) thành UsersScheduleResponseDto
    private UsersScheduleResponseDto mapToResponseDto(UsersSchedule entity) {
        UsersScheduleResponseDto dto = new UsersScheduleResponseDto();
        dto.setId(entity.getId());

        if (entity.getUser() != null) {
            User user = entity.getUser();
            dto.setUserId(Long.valueOf(user.getId()));
            dto.setUserName(user.getFullName());
            dto.setUserEmail(user.getEmail());

            // --- PHẦN CẬP NHẬT QUAN TRỌNG ---
            // Lấy ImageUrl từ User và gán vào DTO
            dto.setUserImageUrl(user.getImageUrl());

            // Lấy Role Name từ User
            if (user.getRole() != null) {
                dto.setRoleName(user.getRole().getName());
            }


            // ---------------------------------
        }

        dto.setShift(entity.getShift());
        dto.setWorkDate(entity.getWorkDate());
        dto.setCheckInTime(entity.getCheckInTime());
        dto.setCheckOutTime(entity.getCheckOutTime());
        dto.setStatus(entity.getStatus());
        dto.setIsLastTask(entity.getIsLastTask());
        dto.setIsActive(entity.getIsActive());

        return dto;
    }
}