package org.aptech.backendmypham.services.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.aptech.backendmypham.dto.ServiceRequestDto;
import org.aptech.backendmypham.dto.ServiceResponseDto;
import org.aptech.backendmypham.exception.ResourceNotFoundException;
import org.aptech.backendmypham.models.Service;
import org.aptech.backendmypham.repositories.ServiceRepository;
import org.aptech.backendmypham.services.ServicesService;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
@RequiredArgsConstructor

public class ServicesServiceImpl implements ServicesService {

    final private ServiceRepository serviceRepository;

    @Override
    public List<ServiceResponseDto> getAllService() {
        return serviceRepository.findAll().stream()
                .map(this::toServiceResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ServiceResponseDto> findById(Integer id) {
        return serviceRepository.findById(id).map(this::toServiceResponse);
    }

    @Override
    public Optional<ServiceResponseDto> findByName(String name) {
        return serviceRepository.findByName(name).map(this::toServiceResponse);
    }

    @Override
    public ServiceResponseDto createService(ServiceRequestDto serviceRequest) {
        Service newService = this.toServiceEntity(serviceRequest);
        newService.setCreatedAt(Instant.now());
        newService.setIsActive(true);
        Service savedService = serviceRepository.save(newService);
        return this.toServiceResponse(savedService);
    }

    @Override
    public ServiceResponseDto updateService(Integer id, ServiceRequestDto serviceRequest) {
        Service existingService = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy dịch vụ với ID = " + id));

        existingService.setName(serviceRequest.getName());
        existingService.setDescription(serviceRequest.getDescription());
        existingService.setPrice(serviceRequest.getPrice());
        existingService.setImageUrl(serviceRequest.getImageUrl());
        existingService.setDuration(serviceRequest.getDuration());

        Service updatedService = serviceRepository.save(existingService);

        return this.toServiceResponse(updatedService);
    }

    @Override
    public void softDeleteService(Integer id) {
        Service service = serviceRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service không tồn tại hoặc đã bị vô hiệu hóa với ID = " + id));

        service.setIsActive(false);
        serviceRepository.save(service);
    }


    private ServiceResponseDto toServiceResponse(Service service) {
        if (service == null) return null;
        ServiceResponseDto dto = new ServiceResponseDto();
        dto.setId(service.getId());
        dto.setName(service.getName());
        dto.setDescription(service.getDescription());
        dto.setPrice(service.getPrice());
        dto.setDuration(service.getDuration());
        dto.setImageUrl(service.getImageUrl());
        dto.setCreatedAt(service.getCreatedAt());
        dto.setIsActive(service.getIsActive());
        return dto;
    }

    private Service toServiceEntity(ServiceRequestDto request) {
        if (request == null) return null;
        Service entity = new Service();
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        entity.setPrice(request.getPrice());
        entity.setDuration(request.getDuration());
        entity.setImageUrl(request.getImageUrl());
        entity.setCreatedAt(Instant.now());
        entity.setIsActive(true);
        return entity;
    }
}