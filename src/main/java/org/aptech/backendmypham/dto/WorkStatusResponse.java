package org.aptech.backendmypham.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.aptech.backendmypham.models.Schedule;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class WorkStatusResponse {
    private boolean hasShift;
    private List<Schedule> schedules;
    private String currentStatus;
    private String message;
}