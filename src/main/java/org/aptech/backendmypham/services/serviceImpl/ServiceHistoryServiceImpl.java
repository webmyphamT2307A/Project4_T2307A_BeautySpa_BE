package org.aptech.backendmypham.services.serviceImpl;

import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.aptech.backendmypham.dto.ServiceHistoryDTO;
import org.aptech.backendmypham.models.Servicehistory;
import org.aptech.backendmypham.repositories.ServiceHistoryRepository;
import org.aptech.backendmypham.services.ServiceHistoryService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServiceHistoryServiceImpl implements ServiceHistoryService {
    private  final ServiceHistoryRepository serviceHistoryRepository;
    @Override
    public List<ServiceHistoryDTO> getAll() {
        return serviceHistoryRepository.findAll().stream()
                .map(this::mapToDTO)
                .sorted((a, b) -> b.getId().compareTo(a.getId()))
                .collect(Collectors.toList());
    }
    @Override
    public List<ServiceHistoryDTO> getHistoryBycustomerId(Integer customerId) {
        return serviceHistoryRepository.findBycustomerId(customerId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    private ServiceHistoryDTO mapToDTO(Servicehistory history) {
        return new ServiceHistoryDTO(
                history.getId(),
                Math.toIntExact(history.getUser().getId()),
                history.getCustomer().getId(),
                history.getAppointment().getId(),
                history.getService().getId(),
                history.getService().getName(),
                history.getService().getPrice(),
                history.getAppointment().getAppointmentDate(),
                history.getNotes(),
                history.getCreatedAt(),
                history.getIsActive()
        );
    }
}
