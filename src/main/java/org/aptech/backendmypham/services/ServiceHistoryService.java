package org.aptech.backendmypham.services;

import org.aptech.backendmypham.dto.GuestServiceHistoryCreateDTO;
import org.aptech.backendmypham.dto.ServiceHistoryDTO;
import org.aptech.backendmypham.models.Servicehistory;

import java.util.List;
import java.util.Map;

public interface ServiceHistoryService {
    public List<ServiceHistoryDTO> getAll();
    public List<ServiceHistoryDTO> getHistoryBycustomerId(Integer customerId);
    ServiceHistoryDTO createGuestServiceHistory(GuestServiceHistoryCreateDTO createDTO);
    List<ServiceHistoryDTO> lookupHistory(String email, String phone);
    Map<String, List<Object>> getMonthlyHistory(Long userId, Integer year, Integer month);
}
