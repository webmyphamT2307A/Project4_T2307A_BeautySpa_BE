package org.aptech.backendmypham.services.serviceImpl;

// Đảm bảo import đúng Transactional của Spring
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
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UsersScheduleServiceImpl implements UsersScheduleService {
    private final UsersScheduleRepository usersScheduleRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public UsersScheduleResponseDto createSchedule(UsersScheduleRequestDto requestDto) {
        User user = userRepository.findById(requestDto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy User với ID: " + requestDto.getUserId()));

        UsersSchedule usersSchedule = mapToEntity(requestDto);
        usersSchedule.setUser(user);

        // Giá trị mặc định từ Entity @ColumnDefault sẽ được áp dụng bởi DB khi insert,
        // nhưng việc set ở đây đảm bảo đối tượng Java có trạng thái đúng trước khi save
        // và hữu ích nếu bạn không muốn dựa hoàn toàn vào DB default cho logic Java.
        if (usersSchedule.getIsActive() == null) {
            usersSchedule.setIsActive(true);
        }
        if (usersSchedule.getIsLastTask() == null) {
            usersSchedule.setIsLastTask(false);
        }

        UsersSchedule savedSchedule = usersScheduleRepository.save(usersSchedule);
        return mapToResponseDto(savedSchedule);
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
        UsersSchedule existingSchedule = usersScheduleRepository.findById(scheduleId) // Lấy cả active và inactive để có thể kích hoạt lại
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy UsersSchedule với ID: " + scheduleId + " để cập nhật"));

        // Kiểm tra nếu userId trong DTO khác với userId hiện tại của schedule (nếu được phép thay đổi user)
        if (requestDto.getUserId() != null && !existingSchedule.getUser().getId().equals(requestDto.getUserId())) {
            User newUser = userRepository.findById(requestDto.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy User mới với ID: " + requestDto.getUserId()));
            existingSchedule.setUser(newUser);
        }

        // Cập nhật các trường từ DTO nếu chúng được cung cấp
        if (requestDto.getShift() != null) {
            existingSchedule.setShift(requestDto.getShift());
        }
        if (requestDto.getWorkDate() != null) {
            existingSchedule.setWorkDate(requestDto.getWorkDate());
        }
        // Cho phép set checkInTime/checkOutTime thành null nếu DTO gửi null
        existingSchedule.setCheckInTime(requestDto.getCheckInTime());
        existingSchedule.setCheckOutTime(requestDto.getCheckOutTime());

        if (requestDto.getStatus() != null) {
            existingSchedule.setStatus(requestDto.getStatus());
        }
        if (requestDto.getIsLastTask() != null) {
            existingSchedule.setIsLastTask(requestDto.getIsLastTask());
        }
        if (requestDto.getIsActive() != null) {
            existingSchedule.setIsActive(requestDto.getIsActive());
        }
        // Không cập nhật createdAt, updatedAt thường được quản lý bởi JPA @UpdateTimestamp

        UsersSchedule updatedSchedule = usersScheduleRepository.save(existingSchedule);
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

            // Lấy Branch Name từ User
            if (user.getBranch() != null) {
                dto.setBranchName(user.getBranch().getName());
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