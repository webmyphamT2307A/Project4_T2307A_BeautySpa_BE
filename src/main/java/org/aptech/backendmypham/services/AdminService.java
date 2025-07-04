package org.aptech.backendmypham.services;

import org.aptech.backendmypham.dto.UserRequestDto;
import org.aptech.backendmypham.models.User;

import java.util.List;

public interface AdminService {


    void createAdmin(UserRequestDto userRequestDto);


    void updateAdmin(Long userId, String fullName, String password, String email, String phone, String address, Integer roleId,String imageUrl,Integer isActive);

    void deleteAdmin(Long userId);

    User findById(Long userId);

    User findByEmail(String email);

    User findByPhoneNumber(String phoneNumber);

    List<User> findAll();
    List<User> findALlDeteleted();



}

