package org.aptech.backendmypham.services.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.aptech.backendmypham.dto.ReviewCreateRequestDTO;
import org.aptech.backendmypham.dto.ReviewDTO;
import org.aptech.backendmypham.dto.ReviewResponseDTO;
import org.aptech.backendmypham.dto.ReviewUpdateRequestDTO;
import org.aptech.backendmypham.exception.ResourceNotFoundException;
import org.aptech.backendmypham.models.Customer;
import org.aptech.backendmypham.models.Review;
import org.aptech.backendmypham.repositories.*;
import org.aptech.backendmypham.services.ReviewService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {
    private static final Logger logger = LoggerFactory.getLogger(ReviewServiceImpl.class);
    private final ReviewRepository reviewRepository;
    private final CustomerRepository customerRepository;
    private final ServiceRepository serviceRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ReviewResponseDTO createReview(Long customerId, ReviewCreateRequestDTO createDTO) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + customerId));

        validateRelatedObject(createDTO.getType(), createDTO.getRelatedId());

        Review review = new Review();
        review.setCustomer(customer);
        review.setRelatedId(createDTO.getRelatedId());
        review.setType(createDTO.getType());
        review.setRating(createDTO.getRating());
        review.setComment(createDTO.getComment());
        review.setCreatedAt(Instant.now());
        review.setIsActive(true);

        Review savedReview = reviewRepository.save(review);
        return convertToResponseDTO(savedReview);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewResponseDTO> getReviewsByRelatedId(Integer relatedId, Pageable pageable) {
        Page<Review> reviewPage = reviewRepository.findByRelatedIdAndIsActiveTrue(relatedId, pageable);
        return reviewPage.map(this::convertToResponseDTO);
    }

    // Các phương thức getReviewById, updateReview, deleteReview không cần thay đổi
    // vì chúng đã được thiết kế để hoạt động với người dùng có định danh (customerId).

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
        logger.info("===== BẮT ĐẦU QUÁ TRÌNH UPDATE REVIEW ID: {} =====", reviewId);

        Optional<Review> anyReview = reviewRepository.findById(reviewId);
        if (anyReview.isEmpty()) {
            logger.error("!!! DEBUG LỖI: Hoàn toàn không tìm thấy review nào có ID = {}", reviewId);
            throw new ResourceNotFoundException("Review not found with id: " + reviewId);
        } else {
            logger.info(">>> DEBUG INFO: Đã tìm thấy review ID = {}. Trạng thái is_active của nó là: {}", reviewId, anyReview.get().getIsActive());
        }

        Review existingReview = reviewRepository.findByIdAndIsActiveTrue(reviewId)
                .orElseThrow(() -> {
                    logger.error("!!! DEBUG LỖI: Tìm thấy review ID {} nhưng nó không active, hoặc query findByIdAndIsActiveTrue bị lỗi.", reviewId);
                    return new ResourceNotFoundException("Review not found or is inactive with id: " + reviewId);
                });

        logger.info(">>> DEBUG INFO: Query thành công! Review ID {} đang active. Bắt đầu kiểm tra quyền sở hữu.", reviewId);

        checkOwnership(existingReview, customerId);
        existingReview.setRating(updateDTO.getRating());
        existingReview.setComment(updateDTO.getComment());
        Review updatedReview = reviewRepository.save(existingReview);

        logger.info("===== KẾT THÚC UPDATE REVIEW ID: {} THÀNH CÔNG =====", reviewId);
        return convertToResponseDTO(updatedReview);
    }

    @Override
    @Transactional
    public void deleteReview(Long customerId, Integer reviewId) {
        Review existingReview = reviewRepository.findByIdAndIsActiveTrue(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));
        checkOwnership(existingReview, customerId);
        existingReview.setIsActive(false);
        reviewRepository.save(existingReview);
    }

    private void validateRelatedObject(String type, Integer relatedId) {
        if (relatedId == null) {
            throw new IllegalArgumentException("Related ID cannot be null.");
        }
        boolean exists;
        if ("service".equalsIgnoreCase(type)) {
            exists = serviceRepository.existsById(relatedId); // Giả sử ID của Service là Long
            if (!exists) {
                throw new ResourceNotFoundException("Service not found with id: " + relatedId);
            }
        } else if ("user".equalsIgnoreCase(type)) {
            exists = userRepository.existsById(relatedId.longValue());
            if (!exists) {
                throw new ResourceNotFoundException("User (Staff) not found with id: " + relatedId);
            }
        } else {
            throw new IllegalArgumentException("Invalid review type specified: " + type);
        }
    }

    private void checkOwnership(Review review, Long customerId) {
        // Log thông tin đầu vào
        logger.info(">>> [checkOwnership] Bắt đầu kiểm tra quyền cho review ID: {}. Người dùng đang đăng nhập có customerId: {} và role: {}",
                review.getId(), customerId);



        // 2. Nếu không phải admin, kiểm tra quyền sở hữu
        logger.info(">>> [checkOwnership] Người dùng không phải Admin. Bắt đầu so sánh quyền sở hữu.");

        // Lấy ID của người viết review một cách an toàn
        Long authorId = Long.valueOf((review.getCustomer() != null) ? review.getCustomer().getId() : null);
        logger.info(">>> [checkOwnership] ID người viết review (Author ID): {}. ID người đang đăng nhập: {}", authorId, customerId);

        if (authorId == null) {
            logger.error("!!! [checkOwnership] Lỗi: Review ID {} không có thông tin khách hàng (customer is null).", review.getId());
            throw new ResourceNotFoundException("Permission check failed: Review has no author information.");
        }

        if (!Objects.equals(authorId, customerId)) {
            logger.error("!!! [checkOwnership] Lỗi quyền truy cập: User ID {} không khớp với author ID {} của review.", customerId, authorId);
            throw new ResourceNotFoundException("You do not have permission to modify this review.");
        }

        logger.info(">>> [checkOwnership] Kiểm tra quyền sở hữu thành công cho review ID: {}", review.getId());
    }
    @Override
    public List<ReviewDTO> findALL() {
        List<Review> reviews = reviewRepository.findAll();
        List<ReviewDTO> dtos = new ArrayList<>();
        for (Review review : reviews) {
            dtos.add(convertToReviewDTO(review));
        }
        return dtos;
    }




    private ReviewResponseDTO convertToResponseDTO(Review review) {
        ReviewResponseDTO dto = new ReviewResponseDTO();
        dto.setId(review.getId());
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setCreatedAt(review.getCreatedAt());

        if (review.getCustomer() != null) {
            dto.setAuthorName(review.getCustomer().getFullName());
            dto.setCustomerId(review.getCustomer().getId());
        } else {
            dto.setAuthorName("Anonymous");
        }

        return dto;
    }
    private ReviewDTO convertToReviewDTO(Review review) {
        ReviewDTO dto = new ReviewDTO();
        dto.setId(review.getId());

        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setCreatedAt(review.getCreatedAt());
        dto.setType(review.getType());
        dto.setRelatedId(review.getRelatedId());
        dto.setActive(review.getIsActive());
        if (review.getCustomer() != null) {
            dto.setAuthorName(review.getCustomer().getFullName());
            dto.setCustomerId(Long.valueOf(review.getCustomer().getId()));
        } else {
            dto.setAuthorName("Anonymous");
        }
        return dto;

    }
}