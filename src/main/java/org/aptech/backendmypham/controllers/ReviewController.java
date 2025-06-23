package org.aptech.backendmypham.controllers;

import io.swagger.v3.oas.annotations.Operation;
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

    @GetMapping("/item/{relatedId}")
    @Operation(summary = "Lấy danh sách đánh giá theo service/user ID")
    public ResponseEntity<ResponseObject> getReviewsByRelatedId(
            @PathVariable Integer relatedId,
            Pageable pageable) {
        try {
            Page<ReviewResponseDTO> reviews = reviewService.getReviewsByRelatedId(relatedId, pageable);

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
    @PreAuthorize("hasRole('CUSTOMER')")
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
    private Long getCurrentCustomerId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // Method 1: If using CustomUserDetails
        if (auth.getPrincipal() instanceof org.aptech.backendmypham.configs.CustomUserDetails) {
            org.aptech.backendmypham.configs.CustomUserDetails userDetails =
                    (org.aptech.backendmypham.configs.CustomUserDetails) auth.getPrincipal();
            return Long.valueOf(userDetails.getId());
        }



        throw new RuntimeException("Unable to extract customer ID from authentication");
    }


    private Long getCurrentStaffId(Principal principal) {
        String email = principal.getName(); // Usually email from JWT

        // Look up staff user by email
        User staff = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Staff not found with email: " + email));

        return staff.getId();
    }
}