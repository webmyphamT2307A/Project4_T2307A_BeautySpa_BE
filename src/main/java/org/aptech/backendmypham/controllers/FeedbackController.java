package org.aptech.backendmypham.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.aptech.backendmypham.configs.CustomUserDetails;
import org.aptech.backendmypham.dto.FeedbackCreateRequestDTO;
import org.aptech.backendmypham.dto.FeedbackResponseDTO;
import org.aptech.backendmypham.dto.ResponseObject;
import org.aptech.backendmypham.enums.Status;
import org.aptech.backendmypham.services.FeedBackService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.relational.core.sql.In;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/feedbacks")
@RequiredArgsConstructor
public class FeedbackController {
    private final FeedBackService feedbackService;

    @GetMapping("")
    public ResponseEntity<ResponseObject> getAllFeedbacks(@ParameterObject Pageable pageable) {
        Page<FeedbackResponseDTO> feedbackPage = feedbackService.getAllFeedback(pageable);
        return ResponseEntity.ok(
                new ResponseObject(Status.SUCCESS, "Feedbacks retrieved successfully.", feedbackPage)
        );
    }
    @PostMapping("/created")
    public ResponseEntity<ResponseObject> createFeedback(@Valid @RequestBody FeedbackCreateRequestDTO requestDTO) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long customerId = null;

        // Kiểm tra nếu người dùng đã đăng nhập thì lấy customerId
        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            customerId = Long.valueOf(userDetails.getId());
        }

        // customerId có thể là null (nếu là khách)
        FeedbackResponseDTO createdFeedback = feedbackService.createFeedback(customerId, requestDTO);

        return new ResponseEntity<>(
                new ResponseObject(Status.SUCCESS, "Your message has been sent successfully!", createdFeedback),
                HttpStatus.CREATED
        );
    }

    @PutMapping("/delete/{id}")
    public ResponseEntity<ResponseObject> deleteFeedback(@PathVariable Integer id) {
        try {
            feedbackService.deleteFeedbackWithId(id);
            return ResponseEntity.ok(
                    new ResponseObject(Status.SUCCESS, "Feedback deleted successfully.", null)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new ResponseObject(Status.ERROR, "Error deleting feedback: " + e.getMessage(), null)
            );
        }
    }
}
