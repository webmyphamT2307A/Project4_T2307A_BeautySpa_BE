package org.aptech.backendmypham.services;

import org.aptech.backendmypham.models.Customer;

import java.util.List;

public interface CustomerService {
    public List<Customer> getALL();
    public Customer findById(Long UiD);
    public void createCustomer(String password, String fullName, String email, String phoneNumber, String address, String imageUrl);
    public void updateCustomer(Long CustomerId, String password, String fullName, String email, String phoneNumber, String address,String imageUrl,Boolean isActive);
    public void deleteCustomer(Long Cid);
}
