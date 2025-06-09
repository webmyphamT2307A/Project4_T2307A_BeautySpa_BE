package org.aptech.backendmypham.services;

import org.aptech.backendmypham.dto.UsersScheduleRequestDto;
import org.aptech.backendmypham.dto.UsersScheduleResponseDto;

import java.time.LocalDate;
import java.util.List;

public interface UsersScheduleService {
    public UsersScheduleResponseDto createSchedule(UsersScheduleRequestDto requestDto);
    public List<UsersScheduleResponseDto> findSchedules(Long userId, LocalDate startDate, LocalDate endDate, Integer month, Integer year, String status);
    public UsersScheduleResponseDto getScheduleById(Integer scheduleId);
    public UsersScheduleResponseDto updateSchedule(Integer scheduleId, UsersScheduleRequestDto requestDto);
    public boolean deleteSchedule(Integer scheduleId);
}
