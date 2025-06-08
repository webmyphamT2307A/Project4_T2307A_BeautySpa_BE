package org.aptech.backendmypham.services;

import org.aptech.backendmypham.dto.ReviewCreateRequestDTO;
import org.aptech.backendmypham.dto.ReviewResponseDTO;
import org.aptech.backendmypham.dto.ReviewUpdateRequestDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewService {
    ReviewResponseDTO createReview(Long customerId, ReviewCreateRequestDTO createDTO);

    Page<ReviewResponseDTO> getReviewsByRelatedId(Integer relatedId, Pageable pageable);
    ReviewResponseDTO getReviewById(Integer reviewId);

    ReviewResponseDTO updateReview(Integer reviewId, ReviewUpdateRequestDTO updateDTO);

    void deleteReview(Integer reviewId);
}
