package org.aptech.backendmypham.services;

import org.aptech.backendmypham.dto.ServiceRequestDto;
import org.aptech.backendmypham.dto.ServiceResponseDto;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ServicesService {


    List<ServiceResponseDto> getAllService();


    Optional<ServiceResponseDto> findById(Integer id);


    Optional<ServiceResponseDto> findByName(String name);


    ServiceResponseDto createService(ServiceRequestDto serviceRequest);


    ServiceResponseDto updateService(Integer id, ServiceRequestDto serviceRequest);


    void softDeleteService(Integer id);
    Map<String, List<Object>> getMonthlyHistory(Long userId, Integer year, Integer month);
}