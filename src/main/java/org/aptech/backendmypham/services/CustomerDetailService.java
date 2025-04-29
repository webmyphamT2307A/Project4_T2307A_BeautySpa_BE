package org.aptech.backendmypham.services;

import org.aptech.backendmypham.dto.*;
import org.aptech.backendmypham.models.Customer;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public interface CustomerDetailService {
    ResponseObject registerCustomer(RegisterRequestDto registerRequestDto);
    ResponseObject loginCustomer(LoginCustomerDto loginCustomerDto);
    ResponseObject logout();
    ResponseObject getCustomerDetail(Long customerId); // ✅ sửa Integer → Long
    ResponseObject updateCustomer(Long id, CustomerDetailResponseDto CustomerDetailResponseDto, MultipartFile file);
    ResponseObject changePasswordCustomer(ChangePasswordCustomerRequestDto changePasswordRequestDto, Long id); // ✅ bổ sung id
}