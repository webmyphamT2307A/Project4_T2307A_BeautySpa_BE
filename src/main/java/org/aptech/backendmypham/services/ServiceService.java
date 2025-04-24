package org.aptech.backendmypham.services;

import org.aptech.backendmypham.models.Service;

import java.util.Optional;

public interface ServiceService {
    Optional<Service> findById(Integer id);
    Optional<Service> findByName(String name);
    Service updateService(Integer id, Service updatedService);
    void softDeleteService(Integer id);
}
