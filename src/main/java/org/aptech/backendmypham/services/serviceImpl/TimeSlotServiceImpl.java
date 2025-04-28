package org.aptech.backendmypham.services.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.aptech.backendmypham.models.Timeslots;
import org.aptech.backendmypham.repositories.TimeSlotsRepository;
import org.aptech.backendmypham.services.TimeSlotService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TimeSlotServiceImpl implements TimeSlotService {
    private final TimeSlotsRepository timeSlotsRepository;

    @Override
    public List<Timeslots> getALlTimeSlot(){
        return timeSlotsRepository.findAll();
    }
}
