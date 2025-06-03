package org.aptech.backendmypham.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@AllArgsConstructor
public class UsersScheduleRequestDto {

    private Long userId;

    private String shift;

    private LocalDate workDate;

    private LocalTime checkInTime;

    private LocalTime checkOutTime;

    @Size(max = 50, message = "Trạng thái không quá 50 ký tự")
    private String status;

    private Boolean isLastTask;

    private Boolean isActive;

}
