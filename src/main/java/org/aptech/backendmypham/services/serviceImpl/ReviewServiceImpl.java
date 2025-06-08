package org.aptech.backendmypham.services.serviceImpl;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.aptech.backendmypham.dto.ReviewCreateRequestDTO;
import org.aptech.backendmypham.dto.ReviewResponseDTO;
import org.aptech.backendmypham.dto.ReviewUpdateRequestDTO;
import org.aptech.backendmypham.exception.ResourceNotFoundException;
import org.aptech.backendmypham.models.Customer;
import org.aptech.backendmypham.models.Review;
import org.aptech.backendmypham.repositories.CustomerRepository;
import org.aptech.backendmypham.repositories.ReviewRepository;
import org.aptech.backendmypham.services.ReviewService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@AllArgsConstructor
public class ReviewServiceImpl implements ReviewService {
    private final ReviewRepository reviewRepository;
    private final CustomerRepository customerRepository;
    @Override
    @Transactional
    public ReviewResponseDTO createReview(Long customerId, ReviewCreateRequestDTO createDTO) {
        // 1. Tìm khách hàng tương ứng
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + customerId));

        // 2. Tạo đối tượng Review từ DTO
        Review review = new Review();
        review.setCustomer(customer);
        review.setRelatedId(createDTO.getRelatedId());
        review.setType(createDTO.getType());
        review.setRating(createDTO.getRating());
        review.setComment(createDTO.getComment());

        // 3. Set các giá trị mặc định khi tạo mới
        review.setCreatedAt(Instant.now());
        review.setIsActive(true);

        // 4. Lưu vào DB
        Review savedReview = reviewRepository.save(review);

        // 5. Chuyển đổi sang ResponseDTO để trả về
        return convertToResponseDTO(savedReview);
    }

    // READ (By related ID)
    @Override
    @Transactional()
    public Page<ReviewResponseDTO> getReviewsByRelatedId(Integer relatedId, Pageable pageable) {
        Page<Review> reviewPage = reviewRepository.findByRelatedIdAndIsActiveTrue(relatedId, pageable);
        return reviewPage.map(this::convertToResponseDTO);
    }

    // READ (By ID)
    @Override
    @Transactional()
    public ReviewResponseDTO getReviewById(Integer reviewId) {
        Review review = reviewRepository.findByIdAndIsActiveTrue(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found or is inactive with id: " + reviewId));
        return convertToResponseDTO(review);
    }

    // UPDATE
    @Override
    @Transactional
    public ReviewResponseDTO updateReview(Integer reviewId, ReviewUpdateRequestDTO updateDTO) {
        // 1. Tìm review đang tồn tại và active
        Review existingReview = reviewRepository.findByIdAndIsActiveTrue(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found or is inactive with id: " + reviewId));

        // Cần thêm logic kiểm tra xem người dùng hiện tại có phải chủ của review này không
        // (Ví dụ: `if (!existingReview.getCustomer().getId().equals(currentUser.getId())) { throw new UnauthorizedException(); }`)

        // 2. Cập nhật các trường cho phép
        existingReview.setRating(updateDTO.getRating());
        existingReview.setComment(updateDTO.getComment());

        // 3. Lưu lại
        Review updatedReview = reviewRepository.save(existingReview);

        // 4. Trả về DTO
        return convertToResponseDTO(updatedReview);
    }

    // DELETE (Soft)
    @Override
    @Transactional
    public void deleteReview(Integer reviewId) {
        // 1. Tìm review đang tồn tại và active
        Review existingReview = reviewRepository.findByIdAndIsActiveTrue(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found or is inactive with id: " + reviewId));



        // 2. Thực hiện xóa mềm: chỉ cần set cờ isActive = false
        existingReview.setIsActive(false);

        // 3. Lưu lại thay đổi
        reviewRepository.save(existingReview);
    }

    // --- Helper Method để chuyển đổi Entity sang DTO ---
    private ReviewResponseDTO convertToResponseDTO(Review review) {
        ReviewResponseDTO dto = new ReviewResponseDTO();
        dto.setId(review.getId());
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setCreatedAt(review.getCreatedAt());
        if (review.getCustomer() != null) {
            dto.setCustomerId(Long.valueOf(review.getCustomer().getId()));
            dto.setCustomerName(review.getCustomer().getFullName());
        }
        return dto;
    }

}
