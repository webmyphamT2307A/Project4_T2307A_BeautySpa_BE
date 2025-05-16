package org.aptech.backendmypham.repositories;

import org.aptech.backendmypham.models.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    boolean existsByEmail(String email);
    Optional<Customer> findByIdAndIsActiveTrue(Long id);
    Optional<Customer> findByEmail(String email); // cần thiết cho chức năng login

    Optional<Customer> findByPhone(String phoneNumber);

    boolean existsByPhone(String phone);
}
