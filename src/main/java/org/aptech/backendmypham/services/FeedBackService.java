package org.aptech.backendmypham.services;

import org.aptech.backendmypham.dto.FeedbackCreateRequestDTO;
import org.aptech.backendmypham.dto.FeedbackResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.relational.core.sql.In;

public interface FeedBackService {
    public FeedbackResponseDTO createFeedback(Long customerId, FeedbackCreateRequestDTO requestDTO);

    Page<FeedbackResponseDTO> getAllFeedback(Pageable pageable);
    public void deleteFeedback(Long customerId, Integer feedbackId);
    void deleteFeedbackWithId(Integer id);
}
