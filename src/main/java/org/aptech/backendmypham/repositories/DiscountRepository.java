package org.aptech.backendmypham.repositories;

import org.aptech.backendmypham.models.Discount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DiscountRepository extends JpaRepository<Discount, Integer> {
    Optional<Discount> findByIdAndIsActiveTrue(Integer id);
}

