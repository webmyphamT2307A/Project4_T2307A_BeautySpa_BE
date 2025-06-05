package org.aptech.backendmypham.repositories;

import org.aptech.backendmypham.models.Salary;
import org.aptech.backendmypham.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SalaryRepository extends JpaRepository<Salary,Integer> {
    Optional<Salary> findByUserAndMonthAndYear(User user, Integer month, Integer year);

    List<Salary> findByUserAndMonthAndYearAndIsActiveTrue(User user, Integer month, Integer year);

    List<Salary> findByUserAndIsActiveTrue(User user);

    List<Salary> findByMonthAndYearAndIsActiveTrue(Integer month, Integer year);

    List<Salary> findByIsActiveTrue();
}
