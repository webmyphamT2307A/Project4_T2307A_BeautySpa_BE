package org.aptech.backendmypham.services;

import org.aptech.backendmypham.models.User;

import java.util.List;

public interface AdminService {


    void createAdmin(String password,String fullName, String email, String phoneNumber, String address, Integer roleId, Integer branchId);


    void updateAdmin(Long userId, String fullName, String password, String email, String phone, String address, Integer roleId, Integer branchId,String imageUrl,Boolean isActive);

    void deleteAdmin(Long userId);

    User findById(Long userId);

    User findByEmail(String email);

    User findByPhoneNumber(String phoneNumber);

    List<User> findAll();



}

