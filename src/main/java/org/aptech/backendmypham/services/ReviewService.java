package org.aptech.backendmypham.services;

import org.aptech.backendmypham.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface ReviewService {

    // ====================== CUSTOMER REVIEW OPERATIONS ======================
    ReviewResponseDTO createReview(Long customerId, ReviewCreateRequestDTO createDTO);
    Page<ReviewResponseDTO> getReviewsByRelatedId(Integer relatedId, Pageable pageable);
    ReviewResponseDTO getReviewById(Integer reviewId);
    ReviewResponseDTO updateReview(Long customerId, Integer reviewId, ReviewUpdateRequestDTO updateDTO);
    void deleteReview(Long customerId, Integer reviewId);
    List<ReviewDTO> findALL();
    Map<String, ReviewResponseDTO> createServiceAndStaffReview(Long customerId, ReviewServiceAndStaffRequestDTO requestDTO);

    ReviewResponseDTO addBusinessReply(Integer reviewId, Long staffId, ReplyCreateRequestDTO replyDTO);


}