package org.aptech.backendmypham.services;

public interface UserService {
    void createUser(String password,String fullName, String email, String phoneNumber, String address, Integer roleId, Integer branchId);
    void updateUser(Long id,String password, String fullName, String email, String phoneNumber, String address, Integer roleId, Integer branchId);
}
