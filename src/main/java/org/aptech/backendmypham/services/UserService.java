package org.aptech.backendmypham.services;

import org.aptech.backendmypham.dto.TechnicianResponseDTO;
import org.aptech.backendmypham.dto.TechnicianSearchCriteriaDTO;
import org.aptech.backendmypham.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public interface UserService {
    List<User> getUsersByRole(String roleName);
    void createUser(String password,String fullName, String email, String phoneNumber, String address, Integer roleId, Integer branchId);
    void updateUser(Long id,String password, String fullName, String email, String phoneNumber, String address, Integer roleId, Integer branchId);
    void deleteUser(Long id);

    Page<TechnicianResponseDTO> findTechnicians(TechnicianSearchCriteriaDTO criteria, Pageable pageable);
}
