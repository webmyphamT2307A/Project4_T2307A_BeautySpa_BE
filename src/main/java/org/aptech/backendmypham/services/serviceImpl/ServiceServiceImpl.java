package org.aptech.backendmypham.services.serviceImpl;

import org.aptech.backendmypham.models.Service;
import org.aptech.backendmypham.repositories.ServiceRepository;
import org.aptech.backendmypham.services.ServiceService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

@org.springframework.stereotype.Service
public class ServiceServiceImpl implements ServiceService {

    @Autowired
    private ServiceRepository serviceRepository;

    @Override
    public Optional<Service> findById(Integer id) {
        return serviceRepository.findById(id);
    }
    @Override
    public Optional<Service> findByName(String name) {
        return serviceRepository.findByName(name);
    }
    @Override
    public Service updateService(Integer id, Service updatedService) {
        Optional<Service> existingServiceOpt = serviceRepository.findById(id);
        if (existingServiceOpt.isPresent()) {
            Service existing = existingServiceOpt.get();
            existing.setName(updatedService.getName());
            existing.setDescription(updatedService.getDescription());
            existing.setPrice(updatedService.getPrice());
            existing.setDuration(updatedService.getDuration());
            // Không update createdAt, isActive ở đây nếu không có yêu cầu
            return serviceRepository.save(existing);
        }
        throw new RuntimeException("Không tìm thấy dịch vụ với ID = " + id);
    }
    @Override
    public void softDeleteService(Integer id) {
        try {
            Optional<Service> serviceOpt = serviceRepository.findByIdAndIsActiveTrue(id);
            if (serviceOpt.isPresent()) {
                Service service = serviceOpt.get();
                service.setIsActive(false); // Xóa mềm
                serviceRepository.save(service);
            } else {
                System.out.println("Service không tồn tại hoặc đã bị vô hiệu hóa.");
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi xóa mềm service: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
