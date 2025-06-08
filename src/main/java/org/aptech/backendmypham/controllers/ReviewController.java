package org.aptech.backendmypham.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

    @Operation(summary = "Tạo một đánh giá mới (cho cả khách và người dùng đăng nhập)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Tạo đánh giá thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ (validation failed)"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy dịch vụ/nhân viên được đánh giá")
    })
    @PostMapping("")
    public ResponseEntity<ResponseObject> createReview(@Valid @RequestBody ReviewCreateRequestDTO createDTO) {
        // 1. Lấy thông tin xác thực một cách an toàn
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long customerId = null;

        // 2. Kiểm tra xem người dùng có thực sự đăng nhập hay không
        if (authentication != null && !(authentication instanceof AnonymousAuthenticationToken) && authentication.isAuthenticated()) {
            // Nếu đã đăng nhập, lấy ID.
            // Giả định rằng principal's name chính là customer ID dưới dạng String.
            // Bạn cần điều chỉnh cho phù hợp với cách bạn cấu hình UserDetails.
            customerId = Long.parseLong(authentication.getName());
        }

        // 3. Gọi service với customerId (có thể là null nếu là khách vãng lai)
        ReviewResponseDTO createdReview = reviewService.createReview(customerId, createDTO);
        return new ResponseEntity<>(
                new ResponseObject(Status.SUCCESS, "", createdReview),
                HttpStatus.CREATED
        );
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
        // 4. Các API update/delete bắt buộc phải xác thực
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || (authentication instanceof AnonymousAuthenticationToken)) {
            // Ném lỗi hoặc trả về response 401 Unauthorized
            return new ResponseEntity<>(new ResponseObject(Status.ERROR, "Yêu cầu đăng nhập để thực hiện chức năng này.", null), HttpStatus.UNAUTHORIZED);
        }
        Long customerId = Long.parseLong(authentication.getName());

        // 5. Gọi service với customerId để kiểm tra quyền
        ReviewResponseDTO updatedReview = reviewService.updateReview(customerId, reviewId, updateDTO);
        return ResponseEntity.ok(
                new ResponseObject(Status.SUCCESS, "", updatedReview)
        );
    }

    @Operation(summary = "Xóa một đánh giá (Yêu cầu đăng nhập)")
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<ResponseObject> deleteReview(
            @Parameter(description = "ID của đánh giá") @PathVariable Integer reviewId
    ) {
        // Tương tự như update, bắt buộc phải xác thực
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || (authentication instanceof AnonymousAuthenticationToken)) {
            return new ResponseEntity<>(new ResponseObject(Status.ERROR, "Yêu cầu đăng nhập để thực hiện chức năng này.", null), HttpStatus.UNAUTHORIZED);
        }
        Long customerId = Long.parseLong(authentication.getName());

        reviewService.deleteReview(customerId, reviewId);
        return ResponseEntity.ok(
                new ResponseObject(Status.SUCCESS, "", null)
        );
    }
}