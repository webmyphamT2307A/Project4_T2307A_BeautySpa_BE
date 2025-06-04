package org.aptech.backendmypham.services.serviceImpl;


import lombok.AllArgsConstructor;
import org.aptech.backendmypham.repositories.SalaryRepository;
import org.aptech.backendmypham.services.SalaryService;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class SalaryServiceImpl implements SalaryService {
    private SalaryRepository salaryRepository;


}
