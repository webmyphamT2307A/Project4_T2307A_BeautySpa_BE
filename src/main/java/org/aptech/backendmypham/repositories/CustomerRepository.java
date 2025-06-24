package org.aptech.backendmypham.repositories;

import org.aptech.backendmypham.models.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    boolean existsByEmail(String email);
    Optional<Customer> findByIdAndIsActiveTrue(Long id);
    Optional<Customer> findByEmail(String email);

    Optional<Customer> findByPhone(String phoneNumber);

    // Method để lấy customer đầu tiên nếu cần
    // Method này GÂY LỖI - trả về Optional nhưng có multiple results
    Optional<Customer> findByEmailOrPhone(String email, String phone);

    // Method này AN TOÀN - lấy result đầu tiên
    @Query("SELECT c FROM Customer c WHERE c.email = :email OR c.phone = :phone ORDER BY c.id ASC")
    Optional<Customer> findFirstByEmailOrPhone(@Param("email") String email, @Param("phone") String phone);

    // Hoặc nếu muốn lấy tất cả results
    @Query("SELECT c FROM Customer c WHERE c.email = :email OR c.phone = :phone")
    List<Customer> findAllByEmailOrPhone(@Param("email") String email, @Param("phone") String phone);
    boolean existsByPhone(String phone);

}
