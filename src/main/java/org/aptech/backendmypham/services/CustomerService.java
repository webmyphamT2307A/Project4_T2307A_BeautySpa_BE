package org.aptech.backendmypham.services;

import org.aptech.backendmypham.dto.CustomerDto;
import org.aptech.backendmypham.models.Customer;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CustomerService {
    public List<Customer> getALL();
    public Customer findById(Long UiD);
    public void createCustomer(CustomerDto customerDto, MultipartFile file);
    public Customer updateCustomer(Long customerId, CustomerDto customerDto, MultipartFile file);
    public void deleteCustomer(Long Cid);
    public Customer createOrGetGuest(CustomerDto guestDto);

}
