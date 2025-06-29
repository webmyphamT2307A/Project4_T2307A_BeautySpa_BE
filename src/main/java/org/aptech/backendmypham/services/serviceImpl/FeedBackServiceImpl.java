package org.aptech.backendmypham.services.serviceImpl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.aptech.backendmypham.dto.FeedbackCreateRequestDTO;
import org.aptech.backendmypham.dto.FeedbackResponseDTO;
import org.aptech.backendmypham.exception.ResourceNotFoundException;
import org.aptech.backendmypham.models.Customer;
import org.aptech.backendmypham.models.Feedback;
import org.aptech.backendmypham.repositories.CustomerRepository;
import org.aptech.backendmypham.repositories.FeedBackRepository;
import org.aptech.backendmypham.services.FeedBackService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class FeedBackServiceImpl implements FeedBackService {
    private final FeedBackRepository feedBackRepository;
    private final CustomerRepository customerRepository;

    @Override
    @Transactional
    public FeedbackResponseDTO createFeedback(Long customerId, FeedbackCreateRequestDTO requestDTO) {
        Feedback feedback = new Feedback();

        // KIỂM TRA: Feedback này từ người dùng đã đăng nhập hay từ khách?
        if (customerId != null) {
            // --- Trường hợp 1: Người dùng đã đăng nhập ---
            Customer customer = customerRepository.findById(customerId)
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + customerId));
            feedback.setCustomer(customer);
        } else {
            // --- Trường hợp 2: Khách vãng lai ---
            feedback.setGuestFirstName(requestDTO.getFirstName());
            feedback.setGuestEmail(requestDTO.getEmail());
            feedback.setGuestPhone(requestDTO.getPhone());
        }

        // Set các thông tin chung
        feedback.setSubject(requestDTO.getSubject());
        feedback.setMessage(requestDTO.getMessage());
        feedback.setCreatedAt(Instant.now());
        feedback.setIsActive(true);

        Feedback savedFeedback = feedBackRepository.save(feedback);
        return convertToResponseDTO(savedFeedback);
    }

    // Cũng cần cập nhật lại hàm convertToResponseDTO
    private FeedbackResponseDTO convertToResponseDTO(Feedback feedback) {
        FeedbackResponseDTO dto = new FeedbackResponseDTO();
        dto.setId(feedback.getId());
        dto.setMessage(feedback.getMessage());
        // Giả sử bạn muốn thêm subject vào response
        // dto.setSubject(feedback.getSubject());
        dto.setCreatedAt(feedback.getCreatedAt());

        // Nếu feedback có customer, lấy tên customer. Nếu không, lấy tên khách.
        if (feedback.getCustomer() != null) {
            dto.setCustomerName(feedback.getCustomer().getFullName());
            dto.setCustomerId(feedback.getCustomer().getId());
        } else {
            dto.setCustomerName(feedback.getGuestFirstName());
        }
        dto.setActive(feedback.getIsActive());

        return dto;
    }
    @Override
    public Page<FeedbackResponseDTO> getAllFeedback(Pageable pageable) {
        Page<Feedback> feedbackPage = feedBackRepository.findAllByIsActiveTrueOrderByIdDesc(pageable);
        return feedbackPage.map(this::convertToResponseDTO);
    }
    @Override
    @Transactional
    public void deleteFeedback(Long customerId, Integer feedbackId) {
        // 1. Tìm feedback theo ID
        Feedback feedback = feedBackRepository.findById(feedbackId)
                .orElseThrow(() -> new ResourceNotFoundException("Feedback not found with id: " + feedbackId));

        if (!Objects.equals(feedback.getCustomer().getId(), customerId)) {
            throw new AccessDeniedException("You do not have permission to delete this feedback.");
        }

        // 3. Thực hiện xóa mềm (soft delete)
        feedback.setIsActive(false);
        feedBackRepository.save(feedback);
    }

    @Override
    public void deleteFeedbackWithId(Integer id) {
        Feedback feedback = feedBackRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy feedback với id: " + id));
        feedback.setIsActive(false);
        feedBackRepository.save(feedback);
    }


}
