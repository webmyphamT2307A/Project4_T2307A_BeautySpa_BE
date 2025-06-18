package org.aptech.backendmypham.repositories;

import org.aptech.backendmypham.models.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeedBackRepository extends JpaRepository<Feedback, Integer> {
}
