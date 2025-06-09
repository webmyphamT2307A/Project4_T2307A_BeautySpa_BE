package org.aptech.backendmypham.services;

import org.aptech.backendmypham.dto.CalculateSalaryRequestDto;
import org.aptech.backendmypham.dto.SalaryRequestDto;
import org.aptech.backendmypham.dto.SalaryResponseDto;

import java.util.List;

public interface SalaryService {
    public SalaryResponseDto calculateAndSaveSalary(CalculateSalaryRequestDto calculateDto);
    public SalaryResponseDto getSalaryById(Integer salaryId);
    public List<SalaryResponseDto> findSalaries(Long userId, Integer month, Integer year);
    public SalaryResponseDto updateSalary(Integer salaryId, SalaryRequestDto requestDto);
    public boolean deleteSalary(Integer salaryId);


}
