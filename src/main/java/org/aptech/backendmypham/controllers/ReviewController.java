package org.aptech.backendmypham.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
@Tag(name = "Review API", description = "API để quản lý đánh giá")
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "Tạo một đánh giá mới")
    @PostMapping("")
    public ResponseEntity<ResponseObject> createReview(
            @Valid @RequestBody ReviewCreateRequestDTO createDTO,
            @AuthenticationPrincipal Long customerId
    ) {
        ReviewResponseDTO createdReview = reviewService.createReview(customerId, createDTO);
        return new ResponseEntity<>(
                new ResponseObject(Status.SUCCESS, "", createdReview),
                HttpStatus.CREATED
        );
    }

    @Operation(summary = "Lấy danh sách đánh giá theo ID liên quan")
    @GetMapping("/item/{relatedId}")
    public ResponseEntity<ResponseObject> getReviewsByRelatedId(
            @Parameter(description = "ID của sản phẩm/bài viết") @PathVariable Integer relatedId,
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

    @Operation(summary = "Cập nhật một đánh giá")
    @PutMapping("/{reviewId}")
    public ResponseEntity<ResponseObject> updateReview(
            @Parameter(description = "ID của đánh giá") @PathVariable Integer reviewId,
            @Valid @RequestBody ReviewUpdateRequestDTO updateDTO
    ) {
        ReviewResponseDTO updatedReview = reviewService.updateReview(reviewId, updateDTO);
        return ResponseEntity.ok(
                new ResponseObject(Status.SUCCESS, "", updatedReview)
        );
    }

    @Operation(summary = "Xóa một đánh giá (Xóa mềm)")
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<ResponseObject> deleteReview(
            @Parameter(description = "ID của đánh giá") @PathVariable Integer reviewId
    ) {
        reviewService.deleteReview(reviewId);
        return ResponseEntity.ok(
                new ResponseObject(Status.SUCCESS, "", null)
        );
    }
}