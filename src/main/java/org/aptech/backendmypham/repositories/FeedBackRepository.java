package org.aptech.backendmypham.repositories;

import org.aptech.backendmypham.models.Feedback;
import org.aptech.backendmypham.models.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;



@Repository
public interface FeedBackRepository extends JpaRepository<Feedback, Integer> {
    Page<Feedback> findAllByIsActiveTrueOrderByIdDesc(Pageable pageable);
}
