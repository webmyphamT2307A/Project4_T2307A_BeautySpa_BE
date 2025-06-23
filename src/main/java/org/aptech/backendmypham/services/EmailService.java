package org.aptech.backendmypham.services;

import org.aptech.backendmypham.dto.EmailConfirmationRequestDto;

public interface EmailService {
    public boolean sendAppointmentConfirmation(EmailConfirmationRequestDto request);

    // THÊM PHƯƠG THỨC NÀY
    boolean sendAppointmentCancellation(EmailConfirmationRequestDto request);

}
