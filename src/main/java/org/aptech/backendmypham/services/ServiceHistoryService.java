package org.aptech.backendmypham.services;

import org.aptech.backendmypham.dto.ServiceHistoryDTO;
import org.aptech.backendmypham.models.Servicehistory;

import java.util.List;

public interface ServiceHistoryService {
    public List<ServiceHistoryDTO> getAll();
    public List<ServiceHistoryDTO> getHistoryBycustomerId(Integer customerId);
}
