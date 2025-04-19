package org.aptech.backendmypham.repositories;

import org.aptech.backendmypham.models.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ServiceRepository extends JpaRepository<Service, Integer> {
    Optional<Service> findByName(String name);
    Optional<Service> findByIdAndIsActiveTrue(Integer id);
}
