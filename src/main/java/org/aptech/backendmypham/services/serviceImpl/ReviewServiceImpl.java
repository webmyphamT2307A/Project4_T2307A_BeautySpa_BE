package org.aptech.backendmypham.services.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.aptech.backendmypham.dto.*;
import org.aptech.backendmypham.exception.ResourceNotFoundException;
import org.aptech.backendmypham.models.Customer;
import org.aptech.backendmypham.models.Review;
import org.aptech.backendmypham.models.ReviewReply;
import org.aptech.backendmypham.models.User;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {
    private static final Logger logger = LoggerFactory.getLogger(ReviewServiceImpl.class);
    private final ReviewRepository reviewRepository;
    private final CustomerRepository customerRepository;
    private final ServiceRepository serviceRepository;
    private final UserRepository userRepository;
    private final ReviewReplyRepository reviewReplyRepository;

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

        Review existingReview = reviewRepository.findByIdAndIsActiveTrue(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found or is inactive with id: " + reviewId));

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

    @Override
    public List<ReviewDTO> findALL() {
        List<Review> reviews = reviewRepository.findAll();
        return reviews.stream()
                .map(this::convertToReviewDTO)
                .collect(Collectors.toList());
    }

    // ====================== STAFF REPLY METHODS ======================
    @Override
    @Transactional
    public ReviewResponseDTO addReplyToReview(Integer reviewId, Long staffId, ReplyCreateRequestDTO replyDTO) {
        Review review = reviewRepository.findByIdAndIsActiveTrue(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));

        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found with id: " + staffId));

        ReviewReply reply = new ReviewReply();
        reply.setReview(review);
        reply.setStaff(staff);
        reply.setComment(replyDTO.getComment());
        reply.setCreatedAt(Instant.now());
        reply.setReplyType("STAFF_TO_CUSTOMER");

        reviewReplyRepository.save(reply);
        return getReviewById(reviewId);
    }

    @Override
    @Transactional
    public ReviewResponseDTO addStaffReplyToReply(Integer parentReplyId, Long staffId, ReplyCreateRequestDTO replyDTO) {
        ReviewReply parentReply = reviewReplyRepository.findById(parentReplyId)
                .orElseThrow(() -> new ResourceNotFoundException("Parent reply not found with id: " + parentReplyId));

        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found with id: " + staffId));

        Review review = parentReply.getReview();

        ReviewReply newReply = new ReviewReply();
        newReply.setReview(review);
        newReply.setParentReply(parentReply);
        newReply.setStaff(staff);
        newReply.setComment(replyDTO.getComment());
        newReply.setCreatedAt(Instant.now());
        newReply.setReplyType("STAFF_TO_CUSTOMER");

        reviewReplyRepository.save(newReply);
        return getReviewById(review.getId());
    }

    // ====================== CUSTOMER REPLY METHODS ======================
    @Override
    @Transactional
    public ReviewResponseDTO addCustomerReplyToReview(Integer reviewId, Long customerId, ReplyCreateRequestDTO replyDTO) {
        Review review = reviewRepository.findByIdAndIsActiveTrue(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + customerId));

        ReviewReply reply = new ReviewReply();
        reply.setReview(review);
        reply.setCustomer(customer);
        reply.setComment(replyDTO.getComment());
        reply.setCreatedAt(Instant.now());
        reply.setReplyType("CUSTOMER_TO_STAFF");

        reviewReplyRepository.save(reply);
        return getReviewById(reviewId);
    }

    @Override
    @Transactional
    public ReviewResponseDTO addCustomerReplyToReply(Integer parentReplyId, Long customerId, ReplyCreateRequestDTO replyDTO) {
        ReviewReply parentReply = reviewReplyRepository.findById(parentReplyId)
                .orElseThrow(() -> new ResourceNotFoundException("Parent reply not found with id: " + parentReplyId));

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + customerId));

        Review review = parentReply.getReview();

        ReviewReply newReply = new ReviewReply();
        newReply.setReview(review);
        newReply.setParentReply(parentReply);
        newReply.setCustomer(customer);
        newReply.setComment(replyDTO.getComment());
        newReply.setCreatedAt(Instant.now());
        newReply.setReplyType("CUSTOMER_TO_STAFF");

        reviewReplyRepository.save(newReply);
        return getReviewById(review.getId());
    }

    // ====================== THREADED REPLY METHODS ======================
    @Override
    @Transactional
    public ReviewResponseDTO addThreadedReply(Integer reviewId, Long userId, String userType, ThreadedReplyCreateRequestDTO replyDTO) {
        Review review = reviewRepository.findByIdAndIsActiveTrue(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));

        ReviewReply reply = new ReviewReply();
        reply.setReview(review);
        reply.setComment(replyDTO.getComment());
        reply.setCreatedAt(Instant.now());

        // Set author based on user type
        if ("STAFF".equals(userType)) {
            User staff = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Staff not found with id: " + userId));
            reply.setStaff(staff);
            reply.setReplyType("STAFF_TO_CUSTOMER");
        } else if ("CUSTOMER".equals(userType)) {
            Customer customer = customerRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + userId));
            reply.setCustomer(customer);
            reply.setReplyType("CUSTOMER_TO_STAFF");
        }

        // Handle nested reply
        if (replyDTO.getParentReplyId() != null) {
            ReviewReply parentReply = reviewReplyRepository.findById(replyDTO.getParentReplyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent reply not found"));
            reply.setParentReply(parentReply);
        }

        reviewReplyRepository.save(reply);
        return getReviewById(reviewId);
    }

    @Override
    @Transactional
    public ReviewResponseDTO addReplyToReply(Integer parentReplyId, Long userId, String userType, ThreadedReplyCreateRequestDTO replyDTO) {
        ReviewReply parentReply = reviewReplyRepository.findById(parentReplyId)
                .orElseThrow(() -> new ResourceNotFoundException("Parent reply not found"));

        Review review = parentReply.getReview();

        ReviewReply newReply = new ReviewReply();
        newReply.setReview(review);
        newReply.setParentReply(parentReply);
        newReply.setComment(replyDTO.getComment());
        newReply.setCreatedAt(Instant.now());

        // Set author based on user type
        if ("STAFF".equals(userType)) {
            User staff = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Staff not found with id: " + userId));
            newReply.setStaff(staff);
            newReply.setReplyType("STAFF_TO_CUSTOMER");
        } else if ("CUSTOMER".equals(userType)) {
            Customer customer = customerRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + userId));
            newReply.setCustomer(customer);
            newReply.setReplyType("CUSTOMER_TO_STAFF");
        }

        reviewReplyRepository.save(newReply);
        return getReviewById(review.getId());
    }

    // ====================== HELPER METHODS ======================
    private void validateRelatedObject(String type, Integer relatedId) {
        if (relatedId == null) {
            throw new IllegalArgumentException("Related ID cannot be null.");
        }
        boolean exists;
        if ("service".equalsIgnoreCase(type)) {
            exists = serviceRepository.existsById(relatedId);
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
        logger.info(">>> [checkOwnership] Bắt đầu kiểm tra quyền cho review ID: {}. Người dùng đang đăng nhập có customerId: {}", review.getId(), customerId);

        Long authorId = (review.getCustomer() != null) ? Long.valueOf(review.getCustomer().getId()) : null;
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

        // Build threaded replies
        dto.setReplies(buildThreadedReplies(review.getReplies()));

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

    private List<ReplyResponseDTO> buildThreadedReplies(List<ReviewReply> replies) {
        if (replies == null || replies.isEmpty()) {
            return new ArrayList<>();
        }

        // Chỉ lấy top-level replies (không có parent)
        return replies.stream()
                .filter(reply -> reply.getParentReply() == null)
                .map(this::convertToReplyDTO)
                .collect(Collectors.toList());
    }

    private ReplyResponseDTO convertToReplyDTO(ReviewReply reply) {
        ReplyResponseDTO dto = new ReplyResponseDTO();
        dto.setId(reply.getId());
        dto.setComment(reply.getComment());
        dto.setReplyType(reply.getReplyType());
        dto.setCreatedAt(reply.getCreatedAt());

        // Set author name
        if (reply.getStaff() != null) {
            dto.setAuthorName(reply.getStaff().getFullName());
        } else if (reply.getCustomer() != null) {
            dto.setAuthorName(reply.getCustomer().getFullName());
        } else {
            dto.setAuthorName("Unknown");
        }

        // Recursively build child replies
        if (reply.getChildReplies() != null && !reply.getChildReplies().isEmpty()) {
            dto.setReplies(reply.getChildReplies().stream()
                    .map(this::convertToReplyDTO)
                    .collect(Collectors.toList()));
        } else {
            dto.setReplies(new ArrayList<>());
        }

        return dto;
    }
}