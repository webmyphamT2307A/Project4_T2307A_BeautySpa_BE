package org.aptech.backendmypham.repositories;

import org.aptech.backendmypham.models.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceRepository extends JpaRepository<Service,Long> {

}
