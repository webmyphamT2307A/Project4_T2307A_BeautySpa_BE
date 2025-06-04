package org.aptech.backendmypham.repositories;

import org.aptech.backendmypham.models.Salary;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SalaryRepository extends JpaRepository<Salary,Integer> {
}
