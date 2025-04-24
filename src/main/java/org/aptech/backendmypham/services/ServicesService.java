package org.aptech.backendmypham.services;

import org.aptech.backendmypham.models.Service;

import java.util.List;
import java.util.Optional;

public interface ServicesService {
    List<Service> getAllService();
    Optional<Service> findById(Integer id);
    Optional<Service> findByName(String name);


    Service updateService(Integer id, Service updatedService);
    void softDeleteService(Integer id);
}
