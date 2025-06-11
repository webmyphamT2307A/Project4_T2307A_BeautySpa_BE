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
import org.aptech.backendmypham.dto.ResponseObject;
import org.aptech.backendmypham.dto.ReviewCreateRequestDTO;
import org.aptech.backendmypham.dto.ReviewResponseDTO;
import org.aptech.backendmypham.dto.ReviewUpdateRequestDTO;
import org.aptech.backendmypham.enums.Status;
import org.aptech.backendmypham.services.ReviewService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
@Tag(name = "Review API", description = "API để quản lý đánh giá")
public class ReviewController {

    private final ReviewService reviewService;

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

        // SỬA LẠI CÁCH LẤY ID GIỐNG NHƯ HÀM createReview
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long customerId = Long.valueOf(userDetails.getId());

        reviewService.deleteReview(customerId, reviewId);
        return ResponseEntity.ok(
                new ResponseObject(Status.SUCCESS, "Xóa đánh giá thành công.", null)
        );
    }
}