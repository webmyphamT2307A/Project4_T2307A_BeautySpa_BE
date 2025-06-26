package org.aptech.backendmypham.repositories;

import org.aptech.backendmypham.models.Review;
import org.aptech.backendmypham.models.ReviewReply;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewReplyRepository extends JpaRepository<ReviewReply, Integer> {
    boolean existsByReviewAndIsActiveTrue(Review review);
}