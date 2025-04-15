package org.aptech.backendmypham.services.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.aptech.backendmypham.repositories.ServiceRepository;
import org.aptech.backendmypham.services.ServicesService;
import org.aptech.backendmypham.models.Service;
import java.util.List;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class ServicesServiceImpl implements ServicesService {
    private  final ServiceRepository serviceRepository;
    @Override
    public List<org.aptech.backendmypham.models.Service> gellALlService(){
        return serviceRepository.findAll();
    }
}
