package org.aptech.backendmypham.repositories;

import org.aptech.backendmypham.models.Discount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface DiscountRepository extends JpaRepository<Discount, Integer> {

    @Modifying
    @Transactional
    @Query("UPDATE Discount d SET d.isActive = false WHERE d.id = :id")
    void softDeleteById(Integer id);

    List<Discount> findByIsActive(Boolean isActive);
}
