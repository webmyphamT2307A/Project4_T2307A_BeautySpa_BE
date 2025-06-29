package org.aptech.backendmypham.services;

import org.aptech.backendmypham.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface ReviewService {

    // ====================== CUSTOMER REVIEW OPERATIONS ======================
    ReviewResponseDTO createReview(Long customerId, ReviewCreateRequestDTO createDTO);
    List<ReviewResponseDTO> getReviewsByTypeAndRelatedId(String type, Integer relatedId);
    ReviewResponseDTO getReviewById(Integer reviewId);
    ReviewResponseDTO updateReview(Long customerId, Integer reviewId, ReviewUpdateRequestDTO updateDTO);
    void deleteReview(Long customerId, Integer reviewId);
    List<ReviewDTO> findALL();
    Page<ReviewDTO> findAllPaged(Integer rating, int page, int size);
    Map<String, ReviewResponseDTO> createServiceAndStaffReview(Long customerId, ReviewServiceAndStaffRequestDTO requestDTO);

    ReviewResponseDTO addBusinessReply(Integer reviewId, Long staffId, ReplyCreateRequestDTO replyDTO);
    void calculateAverageUserRating();

}