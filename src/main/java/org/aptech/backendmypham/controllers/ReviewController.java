package org.aptech.backendmypham.controllers;

import io.swagger.v3.oas.annotations.Operation;
import org.aptech.backendmypham.configs.CustomUserDetails;
import org.aptech.backendmypham.configs.CustomUserDetailsForUser;
import org.aptech.backendmypham.dto.*;
import org.aptech.backendmypham.enums.Status;
import org.aptech.backendmypham.models.User;
import org.aptech.backendmypham.repositories.UserRepository;
import org.aptech.backendmypham.services.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/v1/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private UserRepository userRepository;

    // ====================== CUSTOMER ENDPOINTS ======================

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Tạo đánh giá mới")
    public ResponseEntity<ResponseObject> createReview(
            @Valid @RequestBody ReviewCreateRequestDTO reviewDTO,
            Principal principal) {
        try {
            Long customerId = getCurrentCustomerId();
            ReviewResponseDTO createdReview = reviewService.createReview(customerId, reviewDTO);

            return ResponseEntity.ok(
                    new ResponseObject(Status.SUCCESS, "Review created successfully", createdReview)
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new ResponseObject(Status.FAIL, e.getMessage(), null)
            );
        }
    }

    @GetMapping("/item/{type}/{relatedId}")
    @Operation(summary = "Lấy danh sách đánh giá theo service/user ID")
    public ResponseEntity<ResponseObject> getReviewsByTypeAndRelatedId(
            @PathVariable(name = "type") String type,
            @PathVariable(name = "relatedId") Integer relatedId) {
        try {
            List<ReviewResponseDTO> reviews = reviewService.getReviewsByTypeAndRelatedId(type, relatedId);

            return ResponseEntity.ok(
                    new ResponseObject(Status.SUCCESS, "Reviews retrieved successfully", reviews)
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new ResponseObject(Status.FAIL, e.getMessage(), null)
            );
        }
    }

    @GetMapping("/{reviewId}")
    @Operation(summary = "Lấy chi tiết đánh giá theo ID")
    public ResponseEntity<ResponseObject> getReviewById(@PathVariable Integer reviewId) {
        try {
            ReviewResponseDTO review = reviewService.getReviewById(reviewId);

            return ResponseEntity.ok(
                    new ResponseObject(Status.SUCCESS, "Review retrieved successfully", review)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ResponseObject(Status.FAIL, e.getMessage(), null)
            );
        }
    }

    @PutMapping("/{reviewId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Cập nhật đánh giá (chỉ chủ sở hữu)")
    public ResponseEntity<ResponseObject> updateReview(
            @PathVariable Integer reviewId,
            @Valid @RequestBody ReviewUpdateRequestDTO updateDTO,
            Principal principal) {
        try {
            Long customerId = getCurrentCustomerId();
            ReviewResponseDTO updatedReview = reviewService.updateReview(customerId, reviewId, updateDTO);

            return ResponseEntity.ok(
                    new ResponseObject(Status.SUCCESS, "Review updated successfully", updatedReview)
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new ResponseObject(Status.FAIL, e.getMessage(), null)
            );
        }
    }

    @DeleteMapping("/{reviewId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF') or hasRole('CUSTOMER')")
    @Operation(summary = "Xóa đánh giá (soft delete)")
    public ResponseEntity<ResponseObject> deleteReview(
            @PathVariable Integer reviewId,
            Principal principal) {
        try {
            Long customerId = getCurrentCustomerId();
            reviewService.deleteReview(customerId, reviewId);

            return ResponseEntity.ok(
                    new ResponseObject(Status.SUCCESS, "Review deleted successfully", null)
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new ResponseObject(Status.FAIL, e.getMessage(), null)
            );
        }
    }

    @GetMapping("/findAll")
    @Operation(summary = "Lấy tất cả đánh giá (admin)")
    public ResponseEntity<ResponseObject> getAllReviews() {
        try {
            return ResponseEntity.ok(
                    new ResponseObject(Status.SUCCESS, "All reviews retrieved successfully", reviewService.findALL())
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new ResponseObject(Status.FAIL, e.getMessage(), null)
            );
        }
    }
    @GetMapping("/reviews")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lấy tất cả đánh giá (admin) theo page")
    public ResponseEntity<?> getPagedReviews(
            @RequestParam(required = false) Integer rating,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<ReviewDTO> result = reviewService.findAllPaged(rating, page, size);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/reviews/staff")
    @PreAuthorize("hasRole('STAFF')")
    @Operation(summary = "Lấy tất cả đánh giá (staff) theo page")
    public ResponseEntity<?> getPagedReviewsByUser(
            @RequestParam(required = false) Integer rating,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Principal principal
    ) {
        Long staffId = getCurrentStaffId(principal);
        Page<ReviewDTO> result = reviewService.findAllPagedByUserIdAndTypeStaff(staffId, rating, page, size);
        return ResponseEntity.ok(result);
    }


    // ====================== BUSINESS REPLY ENDPOINT ======================

    /**
     * ✅ NEW: Business reply endpoint for ADMIN/STAFF only
     * Structure: Review → Business Reply (1 level) → End
     */
    @PostMapping("/{reviewId}/reply")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    @Operation(summary = "Thêm phản hồi từ spa (Admin/Staff only)")
    public ResponseEntity<ResponseObject> addBusinessReply(
            @PathVariable Integer reviewId,
            @Valid @RequestBody ReplyCreateRequestDTO replyDTO,
            Principal principal) {
        try {
            Long staffId = getCurrentStaffId(principal);
            ReviewResponseDTO updatedReview = reviewService.addBusinessReply(reviewId, staffId, replyDTO);

            return ResponseEntity.ok(
                    new ResponseObject(Status.SUCCESS, "Business reply added successfully", updatedReview)
            );
        } catch (IllegalStateException e) {
            // Review đã có reply
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    new ResponseObject(Status.FAIL, e.getMessage(), null)
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new ResponseObject(Status.FAIL, e.getMessage(), null)
            );
        }
    }

    // ====================== HELPER METHODS ======================

    /**
     * Extract customer ID from JWT authentication
     * For CUSTOMER role users
     */
    public Long getCurrentCustomerId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();

        if (principal instanceof CustomUserDetails) {
            // User là khách hàng (CUSTOMER)
            return Long.valueOf(((CustomUserDetails) principal).getId()); // trả về kiểu Long hoặc Integer tùy bạn
        } else if (principal instanceof CustomUserDetailsForUser) {
            // User là admin hoặc staff
            return ((CustomUserDetailsForUser) principal).getId();
        } else {
            throw new RuntimeException("Unable to extract customer ID from authentication");
        }
    }

    @PostMapping("/service-and-staff")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Tạo đồng thời đánh giá cho Dịch vụ và Nhân viên")
    public ResponseEntity<ResponseObject> createServiceAndStaffReview(
            @Valid @RequestBody ReviewServiceAndStaffRequestDTO requestDTO) {
        try {
            Long customerId = getCurrentCustomerId();
            Map<String, ReviewResponseDTO> createdReviews = reviewService.createServiceAndStaffReview(customerId, requestDTO);

            return ResponseEntity.ok(
                    new ResponseObject(Status.SUCCESS, "Reviews for service and staff created successfully", createdReviews)
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new ResponseObject(Status.FAIL, e.getMessage(), null)
            );
        }
    }

    @GetMapping("/calculate-average-user")
    @Operation(summary = "Tính rating cho nhân viên, dùng để update nhanh dữ liệu")
    public void calculateAverageUserRating() {
        try {
            reviewService.calculateAverageUserRating();
        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate average user rating: " + e.getMessage());
        }
    }


    private Long getCurrentStaffId(Principal principal) {
        String email = principal.getName(); // Usually email from JWT

        // Look up staff user by email
        User staff = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Staff not found with email: " + email));
        System.out.println("Current staff ID: " + staff.getId());
        return staff.getId();
    }
}