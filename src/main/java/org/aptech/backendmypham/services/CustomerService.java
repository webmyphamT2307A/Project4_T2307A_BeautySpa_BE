package org.aptech.backendmypham.services;

import org.aptech.backendmypham.models.Customer;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CustomerService {
    public List<Customer> getALL();
    public Customer findById(Long UiD);
    public void createCustomer(String password, String fullName, String email, String phoneNumber, String address, MultipartFile file);
    public void updateCustomer(Long CustomerId, String password, String fullName, String email, String phoneNumber, String address,Boolean isActive,String imageUrl,MultipartFile file);
    public void deleteCustomer(Long Cid);
}
