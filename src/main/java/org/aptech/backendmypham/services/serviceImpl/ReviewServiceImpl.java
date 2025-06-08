package org.aptech.backendmypham.services.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.aptech.backendmypham.dto.ReviewCreateRequestDTO;
import org.aptech.backendmypham.dto.ReviewResponseDTO;
import org.aptech.backendmypham.dto.ReviewUpdateRequestDTO;
import org.aptech.backendmypham.exception.ResourceNotFoundException;
import org.aptech.backendmypham.models.Customer;
import org.aptech.backendmypham.models.Review;
import org.aptech.backendmypham.repositories.*;
import org.aptech.backendmypham.services.ReviewService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Dùng của Spring

import java.time.Instant;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {
    private final ReviewRepository reviewRepository;
    private final CustomerRepository customerRepository;
    private final ServiceRepository serviceRepository; // Giả sử có repo này
    private final UserRepository userRepository; // Giả sử có repo này

    @Override
    @Transactional
    public ReviewResponseDTO createReview(Long customerId, ReviewCreateRequestDTO createDTO) {
        // 1. (Validation) Kiểm tra đối tượng được đánh giá có tồn tại không
        validateRelatedObject(createDTO.getType(), createDTO.getRelatedId());

        Review review = new Review();
        review.setRelatedId(createDTO.getRelatedId());
        review.setType(createDTO.getType());
        review.setRating(createDTO.getRating());
        review.setComment(createDTO.getComment());
        review.setCreatedAt(Instant.now());
        review.setIsActive(true);

        // 2. (Logic) Xử lý cho người dùng đăng nhập hoặc khách vãng lai
        if (customerId != null) {
            Customer customer = customerRepository.findById(customerId)
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + customerId));
            review.setCustomer(customer);
        } else {
            if (createDTO.getGuestName() == null || createDTO.getGuestName().isBlank()) {
                throw new IllegalArgumentException("Guest name is required for anonymous reviews.");
            }
            review.setGuestName(createDTO.getGuestName());
            review.setGuestEmail(createDTO.getGuestEmail());
        }

        Review savedReview = reviewRepository.save(review);
        return convertToResponseDTO(savedReview);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewResponseDTO> getReviewsByRelatedId(Integer relatedId, Pageable pageable) {
        Page<Review> reviewPage = reviewRepository.findByRelatedIdAndIsActiveTrue(relatedId, pageable);
        return reviewPage.map(this::convertToResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewResponseDTO getReviewById(Integer reviewId) {
        Review review = reviewRepository.findByIdAndIsActiveTrue(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found or is inactive with id: " + reviewId));
        return convertToResponseDTO(review);
    }

    @Override
    @Transactional
    public ReviewResponseDTO updateReview(Long customerId, Integer reviewId, ReviewUpdateRequestDTO updateDTO) {
        Review existingReview = reviewRepository.findByIdAndIsActiveTrue(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));

        // 3. (Bảo mật) Kiểm tra quyền sở hữu
        checkOwnership(existingReview, customerId);

        existingReview.setRating(updateDTO.getRating());
        existingReview.setComment(updateDTO.getComment());
        Review updatedReview = reviewRepository.save(existingReview);
        return convertToResponseDTO(updatedReview);
    }

    @Override
    @Transactional
    public void deleteReview(Long customerId, Integer reviewId) {
        Review existingReview = reviewRepository.findByIdAndIsActiveTrue(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));

        // 3. (Bảo mật) Kiểm tra quyền sở hữu
        checkOwnership(existingReview, customerId);

        existingReview.setIsActive(false);
        reviewRepository.save(existingReview);
    }

    private void validateRelatedObject(String type, Integer relatedId) {
        if ("SERVICE".equalsIgnoreCase(type)) {
            if (!serviceRepository.existsById(relatedId)) {
                throw new ResourceNotFoundException("Service with id " + relatedId + " not found.");
            }
        } else if ("EMPLOYEE".equalsIgnoreCase(type)) {
            if (!userRepository.existsById(Long.valueOf(relatedId))) {
                throw new ResourceNotFoundException("Employee with id " + relatedId + " not found.");
            }
        } else {
            throw new IllegalArgumentException("Invalid review type: " + type);
        }
    }

    private void checkOwnership(Review review, Long customerId) {
        if (review.getCustomer() == null || !Objects.equals(review.getCustomer().getId(), customerId)) {
            // Có thể kiểm tra thêm nếu là ADMIN thì cho qua
            throw new ResourceNotFoundException("You do not have permission to modify this review.");
        }
    }

    private ReviewResponseDTO convertToResponseDTO(Review review) {
        ReviewResponseDTO dto = new ReviewResponseDTO();
        dto.setId(review.getId());
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setCreatedAt(review.getCreatedAt());

        if (review.getCustomer() != null) {
            dto.setAuthorName(review.getCustomer().getFullName());
        } else {
            dto.setAuthorName(review.getGuestName());
        }
        return dto;
    }
}