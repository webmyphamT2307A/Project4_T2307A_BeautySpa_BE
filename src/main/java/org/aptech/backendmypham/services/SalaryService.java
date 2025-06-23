package org.aptech.backendmypham.services;

import org.aptech.backendmypham.dto.CalculateSalaryRequestDto;
import org.aptech.backendmypham.dto.SalaryDetails;
import org.aptech.backendmypham.dto.SalaryRequestDto;
import org.aptech.backendmypham.dto.SalaryResponseDto;

import java.util.List;

public interface SalaryService {
    SalaryResponseDto calculateAndSaveSalary(CalculateSalaryRequestDto calculateDto);
    SalaryResponseDto getSalaryById(Integer salaryId);
    List<SalaryResponseDto> findSalaries(Long userId, Integer month, Integer year);
    SalaryResponseDto updateSalary(Integer salaryId, SalaryRequestDto requestDto);
    boolean deleteSalary(Integer salaryId);
    SalaryDetails getEstimatedSalary(Long userId);

}
