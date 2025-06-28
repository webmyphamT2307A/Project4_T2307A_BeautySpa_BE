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
import java.util.Map;
import java.util.Objects;
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
    public List<ReviewResponseDTO> getReviewsByTypeAndRelatedId(String type, Integer relatedId) {
        List<Review> reviewPage = reviewRepository.findByRelatedIdAndTypeAndIsActiveTrue(relatedId, type );
        return reviewPage.stream()
                .map(this::convertToResponseDTO)
                .toList();
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

    // ====================== BUSINESS REPLY METHOD (SIMPLIFIED) ======================
    @Override
    @Transactional
    public ReviewResponseDTO addBusinessReply(Integer reviewId, Long staffId, ReplyCreateRequestDTO replyDTO) {
        Review review = reviewRepository.findByIdAndIsActiveTrue(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));

        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found with id: " + staffId));

        // Optional: Kiểm tra xem review đã có business reply chưa (1 reply per review)
        boolean hasExistingReply = reviewReplyRepository.existsByReviewAndIsActiveTrue(review);
        if (hasExistingReply) {
            throw new IllegalStateException("Review này đã có phản hồi từ spa");
        }

        ReviewReply reply = new ReviewReply();
        reply.setReview(review);
        reply.setStaff(staff);
        reply.setComment(replyDTO.getComment());
        reply.setCreatedAt(Instant.now());
        reply.setReplyType("STAFF_TO_CUSTOMER");
        reply.setIsActive(true);

        reviewReplyRepository.save(reply);
        return getReviewById(reviewId);
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
        } else if ("staff".equalsIgnoreCase(type)) {
            exists = userRepository.existsById(relatedId.longValue());
            if (!exists) {
                throw new ResourceNotFoundException("User (Staff) not found with id: " + relatedId);
            }
        } else {
            throw new IllegalArgumentException("Invalid review type specified: " + type);
        }
    }

    public void calculateAverageUserRating(){
        List<Review> reviews = reviewRepository.findAllByType("user");
        if (reviews.isEmpty()) {
            return; // Không có review nào
        }
        //tìm tất cả user có review (tìm theo relatedId)
        List<Integer> userIds = reviews.stream()
                .map(Review::getRelatedId)
                .distinct()
                .collect(Collectors.toList());
        System.out.println("User IDs with reviews: " + userIds);
        //tính rating trung bình cho từng user
        for (Integer userId : userIds) {
            List<Review> userReviews = reviews.stream()
                    .filter(review -> review.getRelatedId().equals(userId))
                    .collect(Collectors.toList());
            double averageRating = userReviews.stream()
                    .mapToInt(Review::getRating)
                    .average()
                    .orElse(0.0);
            averageRating = Math.round(averageRating * 10.0) / 10.0;
            // Cập nhật rating trung bình cho user
            updateAverageRating("user", userId, averageRating, userReviews.size());
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
    @Override
    @Transactional
    public Map<String, ReviewResponseDTO> createServiceAndStaffReview(Long customerId, ReviewServiceAndStaffRequestDTO requestDTO) {

        // 1. Tạo review cho Dịch vụ
        ReviewCreateRequestDTO serviceReviewDTO = new ReviewCreateRequestDTO();
        serviceReviewDTO.setType("service"); // Đặt type là "service"
        serviceReviewDTO.setRelatedId(requestDTO.getServiceId());
        serviceReviewDTO.setRating(requestDTO.getServiceRating());
        serviceReviewDTO.setComment(requestDTO.getComment());

        // Tái sử dụng logic createReview đã có
        ReviewResponseDTO createdServiceReview = this.createReview(customerId, serviceReviewDTO);

        // 2. Tạo review cho Nhân viên (Staff)
        ReviewCreateRequestDTO staffReviewDTO = new ReviewCreateRequestDTO();
        staffReviewDTO.setType("staff"); // Đặt type là "user"
        staffReviewDTO.setRelatedId(requestDTO.getStaffId().intValue()); // Cần chuyển Long sang Integer cho relatedId
        staffReviewDTO.setRating(requestDTO.getStaffRating());
        staffReviewDTO.setComment(requestDTO.getComment());

        // Tái sử dụng logic createReview đã có
        ReviewResponseDTO createdStaffReview = this.createReview(customerId, staffReviewDTO);

        // Tính toán lại rating trung bình cho staff (dịch vụ được tính ở fe)
        calculateAverageRating(requestDTO.getStaffId().intValue(), "staff");
        // 3. Trả về cả hai review vừa tạo
        return Map.of(
                "serviceReview", createdServiceReview,
                "staffReview", createdStaffReview
        );
    }

    private void calculateAverageRating(Integer relatedId, String type) {
        List<Review> reviews = reviewRepository.findByRelatedIdAndType(relatedId, type);
        if (reviews.isEmpty()) {
            return;
        }

        double averageRating = reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
        averageRating = Math.round(averageRating * 10.0) / 10.0;
        // Cập nhật rating trung bình cho đối tượng liên quan (Service hoặc User)
        updateAverageRating(type,relatedId, averageRating, reviews.size());

    }

    private void updateAverageRating(String type, Integer relatedId, double averageRating, int reviewCount) {
        //service được tính ở fe, nên chỉ cần tính của user
        if ("staff".equalsIgnoreCase(type)) {
            User user = userRepository.findById(relatedId.longValue())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy user với id: " + relatedId));
            user.setAverageRating(averageRating);
            user.setTotalReviews(reviewCount);
            userRepository.save(user);
        } else {
            throw new IllegalArgumentException("Loại đánh giá không hợp lệ: " + type);
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
            dto.setCustomerId(review.getCustomer().getId());
        } else {
            dto.setAuthorName("Anonymous");
        }

        // ✅ SIMPLIFIED: Chỉ business replies (không threading)
        dto.setReplies(buildBusinessReplies(review.getReplies()));
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

    private List<ReplyResponseDTO> buildBusinessReplies(List<ReviewReply> replies) {
        if (replies == null || replies.isEmpty()) {
            return new ArrayList<>();
        }

        // Chỉ lấy active business replies từ staff/admin
        return replies.stream()
                .filter(reply -> reply.getIsActive() && "STAFF_TO_CUSTOMER".equals(reply.getReplyType()))
                .map(this::convertToReplyDTO)
                .collect(Collectors.toList());
    }

    private ReplyResponseDTO convertToReplyDTO(ReviewReply reply) {
        ReplyResponseDTO dto = new ReplyResponseDTO();
        dto.setId(reply.getId());
        dto.setComment(reply.getComment());
        dto.setReplyType(reply.getReplyType());
        dto.setCreatedAt(reply.getCreatedAt());

        // Set author name (chỉ staff)
        if (reply.getStaff() != null) {
            dto.setAuthorName(reply.getStaff().getFullName());
        } else {
            dto.setAuthorName("Unknown Staff");
        }

        return dto;
    }
}