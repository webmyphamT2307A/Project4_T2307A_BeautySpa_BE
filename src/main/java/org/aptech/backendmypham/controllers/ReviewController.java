package org.aptech.backendmypham.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.aptech.backendmypham.configs.CustomUserDetails;
import org.aptech.backendmypham.dto.*;
import org.aptech.backendmypham.enums.Status;
import org.aptech.backendmypham.models.ReviewReply;
import org.aptech.backendmypham.models.User;
import org.aptech.backendmypham.repositories.UserRepository;
import org.aptech.backendmypham.services.ReviewService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Collection;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
@Tag(name = "Review API", description = "API để quản lý đánh giá")
public class ReviewController {

    private final ReviewService reviewService;
    private final UserRepository userRepository;

    @Operation(
            summary = "Tạo một đánh giá mới (Yêu cầu đăng nhập)",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Tạo đánh giá thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy đối tượng được đánh giá")
    })
    @PostMapping("")
    public ResponseEntity<ResponseObject> createReview(@Valid @RequestBody ReviewCreateRequestDTO createDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || (authentication instanceof AnonymousAuthenticationToken)) {
            return new ResponseEntity<>(new ResponseObject(Status.ERROR, "Yêu cầu đăng nhập...", null), HttpStatus.UNAUTHORIZED);
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long customerId = Long.valueOf(userDetails.getId());

        ReviewResponseDTO createdReview = reviewService.createReview(customerId, createDTO);
        return new ResponseEntity<>(new ResponseObject(Status.SUCCESS, "...", createdReview), HttpStatus.CREATED);
    }

    @Operation(summary = "Lấy danh sách đánh giá theo ID liên quan")
    @GetMapping("/item/{relatedId}")
    public ResponseEntity<ResponseObject> getReviewsByRelatedId(
            @Parameter(description = "ID của dịch vụ/nhân viên") @PathVariable Integer relatedId,
            @ParameterObject Pageable pageable
    ) {
        Page<ReviewResponseDTO> reviewPage = reviewService.getReviewsByRelatedId(relatedId, pageable);
        return ResponseEntity.ok(
                new ResponseObject(Status.SUCCESS, "", reviewPage)
        );
    }

    @Operation(summary = "Lấy chi tiết một đánh giá")
    @GetMapping("/{reviewId}")
    public ResponseEntity<ResponseObject> getReviewById(
            @Parameter(description = "ID của đánh giá") @PathVariable Integer reviewId
    ) {
        ReviewResponseDTO review = reviewService.getReviewById(reviewId);
        return ResponseEntity.ok(
                new ResponseObject(Status.SUCCESS, "", review)
        );
    }

    @Operation(summary = "Cập nhật một đánh giá (Yêu cầu đăng nhập)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
            @ApiResponse(responseCode = "403", description = "Không có quyền sửa đánh giá này"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy đánh giá")
    })
    @PutMapping("/{reviewId}")
    public ResponseEntity<ResponseObject> updateReview(
            @Parameter(description = "ID của đánh giá") @PathVariable Integer reviewId,
            @Valid @RequestBody ReviewUpdateRequestDTO updateDTO
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || (authentication instanceof AnonymousAuthenticationToken)) {
            return new ResponseEntity<>(new ResponseObject(Status.ERROR, "Yêu cầu đăng nhập để thực hiện chức năng này.", null), HttpStatus.UNAUTHORIZED);
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long customerId = Long.valueOf(userDetails.getId());

        ReviewResponseDTO updatedReview = reviewService.updateReview(customerId, reviewId, updateDTO);
        return ResponseEntity.ok(
                new ResponseObject(Status.SUCCESS, "Cập nhật đánh giá thành công.", updatedReview)
        );
    }

    @Operation(summary = "Xóa một đánh giá (Yêu cầu đăng nhập)")
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<ResponseObject> deleteReview(
            @Parameter(description = "ID của đánh giá") @PathVariable Integer reviewId
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || (authentication instanceof AnonymousAuthenticationToken)) {
            return new ResponseEntity<>(new ResponseObject(Status.ERROR, "Yêu cầu đăng nhập để thực hiện chức năng này.", null), HttpStatus.UNAUTHORIZED);
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long customerId = Long.valueOf(userDetails.getId());

        reviewService.deleteReview(customerId, reviewId);
        return ResponseEntity.ok(
                new ResponseObject(Status.SUCCESS, "Xóa đánh giá thành công.", null)
        );
    }

    @GetMapping("/findAll")
    @Operation(summary = "Lấy hết review của khách")
    public ResponseEntity<ResponseObject> findAll() {
        return ResponseEntity.ok(
                new ResponseObject(Status.SUCCESS, "Thành công", reviewService.findALL())
        );
    }

    @Operation(
            summary = "Thêm phản hồi cho một đánh giá (Cho phép tất cả user đã đăng nhập)",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/{reviewId}/reply")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF') or hasRole('CUSTOMER')")
    public ResponseEntity<ResponseObject> addReply(
            @PathVariable Integer reviewId,
            @Valid @RequestBody ReplyCreateRequestDTO replyDTO,
            Authentication authentication
    ) {
        try {
            // Xác định loại user và userId
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            boolean isStaff = authorities.contains(new SimpleGrantedAuthority("ROLE_ADMIN")) ||
                    authorities.contains(new SimpleGrantedAuthority("ROLE_STAFF"));

            ReviewResponseDTO updatedReview;

            if (isStaff) {
                // Staff/Admin reply
                String username = authentication.getName();
                User staff = userRepository.findByEmail(username)
                        .orElseThrow(() -> new RuntimeException("Staff not found"));
                updatedReview = reviewService.addReplyToReview(reviewId, staff.getId(), replyDTO);
            } else {
                // Customer reply
                CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
                Long customerId = Long.valueOf(userDetails.getId());
                updatedReview = reviewService.addCustomerReplyToReview(reviewId, customerId, replyDTO);
            }

            return new ResponseEntity<>(
                    new ResponseObject(Status.SUCCESS, "Đã gửi phản hồi.", updatedReview),
                    HttpStatus.CREATED
            );

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new ResponseObject(Status.ERROR, e.getMessage(), null)
            );
        }
    }

    @Operation(summary = "Thêm phản hồi đa cấp cho review")
    @PostMapping("/{reviewId}/threaded-reply")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF') or hasRole('CUSTOMER')")
    public ResponseEntity<ResponseObject> addThreadedReply(
            @PathVariable Integer reviewId,
            @Valid @RequestBody ThreadedReplyCreateRequestDTO replyDTO,
            Authentication authentication
    ) {
        try {
            String userType;
            Long userId;

            // Determine user type and ID
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            boolean isStaff = authorities.contains(new SimpleGrantedAuthority("ROLE_ADMIN")) ||
                    authorities.contains(new SimpleGrantedAuthority("ROLE_STAFF"));

            if (isStaff) {
                String username = authentication.getName();
                User staff = userRepository.findByEmail(username)
                        .orElseThrow(() -> new RuntimeException("Staff not found"));
                userType = "STAFF";
                userId = staff.getId();
            } else {
                CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
                userType = "CUSTOMER";
                userId = Long.valueOf(userDetails.getId());
            }

            ReviewResponseDTO updatedReview = reviewService.addThreadedReply(reviewId, userId, userType, replyDTO);
            return new ResponseEntity<>(
                    new ResponseObject(Status.SUCCESS, "Đã gửi phản hồi.", updatedReview),
                    HttpStatus.CREATED
            );

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new ResponseObject(Status.ERROR, e.getMessage(), null)
            );
        }
    }

    @PostMapping("/replies/{replyId}/reply")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF') or hasRole('CUSTOMER')")
    public ResponseEntity<ResponseObject> addReplyToReply(
            @PathVariable Integer replyId,
            @RequestBody ReplyCreateRequestDTO replyRequest,
            Authentication authentication) {

        try {
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            boolean isStaff = authorities.contains(new SimpleGrantedAuthority("ROLE_ADMIN")) ||
                    authorities.contains(new SimpleGrantedAuthority("ROLE_STAFF"));

            ReviewResponseDTO updatedReview;

            if (isStaff) {
                // Staff reply to reply
                String username = authentication.getName();
                User staff = userRepository.findByEmail(username)
                        .orElseThrow(() -> new RuntimeException("Staff not found"));
                updatedReview = reviewService.addStaffReplyToReply(replyId, staff.getId(), replyRequest);
            } else {
                // Customer reply to reply
                CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
                Long customerId = Long.valueOf(userDetails.getId());
                updatedReview = reviewService.addCustomerReplyToReply(replyId, customerId, replyRequest);
            }

            return ResponseEntity.ok(
                    new ResponseObject(Status.SUCCESS, "Phản hồi đã được thêm thành công", updatedReview)
            );

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new ResponseObject(Status.ERROR, e.getMessage(), null)
            );
        }
    }
}