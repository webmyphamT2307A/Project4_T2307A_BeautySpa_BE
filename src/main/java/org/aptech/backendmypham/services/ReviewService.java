package org.aptech.backendmypham.services;

import org.aptech.backendmypham.dto.ReviewCreateRequestDTO;
import org.aptech.backendmypham.dto.ReviewResponseDTO;
import org.aptech.backendmypham.dto.ReviewUpdateRequestDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewService {
    // customerId có thể là null (cho khách vãng lai)
    ReviewResponseDTO createReview(Long customerId, ReviewCreateRequestDTO createDTO);

    Page<ReviewResponseDTO> getReviewsByRelatedId(Integer relatedId, Pageable pageable);

    ReviewResponseDTO getReviewById(Integer reviewId);

    // Cần customerId để kiểm tra quyền
    ReviewResponseDTO updateReview(Long customerId, Integer reviewId, ReviewUpdateRequestDTO updateDTO);

    // Cần customerId để kiểm tra quyền
    void deleteReview(Long customerId, Integer reviewId);
}
