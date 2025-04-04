package org.aptech.backendmypham.repositories;

import org.aptech.backendmypham.models.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BranchRepository extends JpaRepository<Branch, Long> {
    @Query("SELECT b FROM Branch b WHERE b.id = :id AND b.isActive = true")
    Optional<Branch> findByIdAndIsActiveTrue(Long id);
}
