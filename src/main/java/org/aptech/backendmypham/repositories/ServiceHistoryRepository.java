package org.aptech.backendmypham.repositories;

import org.aptech.backendmypham.models.Servicehistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ServiceHistoryRepository extends JpaRepository<Servicehistory,Integer> {
    @Query("SELECT h FROM Servicehistory h WHERE h.customer.id = :customerId")
    List<Servicehistory> findBycustomerId(@Param("customerId") Integer customerId);
}
