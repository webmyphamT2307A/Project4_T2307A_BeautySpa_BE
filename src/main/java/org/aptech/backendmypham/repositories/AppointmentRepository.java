package org.aptech.backendmypham.repositories;

import org.aptech.backendmypham.models.Appointment;
import org.aptech.backendmypham.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    Optional<Appointment> findByIdAndIsActive(Long Aid, Boolean isActive);


    int countByAppointmentDateBetweenAndServiceIdAndTimeSlot_SlotId(
            Instant start, Instant end, Long serviceId, Long slotId
    );
}
