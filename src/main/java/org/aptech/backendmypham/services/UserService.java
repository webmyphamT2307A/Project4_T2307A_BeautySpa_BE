package org.aptech.backendmypham.services;

import org.aptech.backendmypham.models.User;

import java.util.List;

public interface UserService {
    List<User> getUsersByRole(String roleName);
    void createUser(String password,String fullName, String email, String phoneNumber, String address, Integer roleId, Integer branchId);
    void updateUser(Long id,String password, String fullName, String email, String phoneNumber, String address, Integer roleId, Integer branchId);
    void deleteUser(Long id);
}
