package org.aptech.backendmypham.repositories;

import org.aptech.backendmypham.models.Servicehistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ServiceHistoryRepository extends JpaRepository<Servicehistory,Integer> {
    @Query("SELECT h FROM Servicehistory h WHERE h.customer.id = :customerId")
    List<Servicehistory> findBycustomerId(@Param("customerId") Integer customerId);

    List<Servicehistory> findByCustomer_Id(Integer customerId);


//    findByAppointmentIdAndIsActiveTrue
    @Query("SELECT h FROM Servicehistory h WHERE h.appointment.id = :appointmentId AND h.isActive = true")
    List<Servicehistory> findByAppointmentIdAndIsActiveTrue(@Param("appointmentId") Integer appointmentId);

    @Query("SELECT h FROM Servicehistory h WHERE h.appointment.id = :appointmentId")
    List<Servicehistory> findByAppointmentId(@Param("appointmentId") Integer appointmentId);
}
