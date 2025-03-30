package org.aptech.backendmypham.services;

public interface UserService {
    void createUser(String password, String email, String phoneNumber, String address, Integer roleId, Integer branchId);
}
