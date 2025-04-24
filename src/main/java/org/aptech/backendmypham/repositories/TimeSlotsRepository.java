package org.aptech.backendmypham.repositories;

import org.aptech.backendmypham.models.Timeslots;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TimeSlotsRepository extends JpaRepository<Timeslots,Long> {
}
