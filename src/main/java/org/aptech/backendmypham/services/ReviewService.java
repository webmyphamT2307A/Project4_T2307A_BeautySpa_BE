package org.aptech.backendmypham.services;

import org.aptech.backendmypham.dto.*;
import org.aptech.backendmypham.models.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ReviewService {
    // customerId có thể là null (cho khách vãng lai)
    ReviewResponseDTO createReview(Long customerId, ReviewCreateRequestDTO createDTO);

    Page<ReviewResponseDTO> getReviewsByRelatedId(Integer relatedId, Pageable pageable);

    ReviewResponseDTO getReviewById(Integer reviewId);

    // Cần customerId để kiểm tra quyền
    ReviewResponseDTO updateReview(Long customerId, Integer reviewId, ReviewUpdateRequestDTO updateDTO);

    // Cần customerId để kiểm tra quyền
    void deleteReview(Long customerId, Integer reviewId);
    ReviewResponseDTO addReplyToReview(Integer reviewId, Long staffId, ReplyCreateRequestDTO replyDTO);
    List<ReviewDTO> findALL();
    ReviewResponseDTO addThreadedReply(Integer reviewId, Long userId, String userType, ThreadedReplyCreateRequestDTO replyDTO);
    ReviewResponseDTO addReplyToReply(Integer parentReplyId, Long userId, String userType, ThreadedReplyCreateRequestDTO replyDTO);
    public ReviewResponseDTO addStaffReplyToReply(Integer parentReplyId, Long staffId, ReplyCreateRequestDTO replyDTO);
    public ReviewResponseDTO addCustomerReplyToReply(Integer parentReplyId, Long customerId, ReplyCreateRequestDTO replyDTO);

    public ReviewResponseDTO addCustomerReplyToReview(Integer reviewId, Long customerId, ReplyCreateRequestDTO replyDTO);

}
