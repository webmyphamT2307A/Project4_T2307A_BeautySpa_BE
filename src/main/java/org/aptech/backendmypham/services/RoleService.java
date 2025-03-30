package org.aptech.backendmypham.services;

import org.aptech.backendmypham.models.Role;

import java.util.List;

public interface RoleService {
    void createRole(String roleName);

    void deleteRole(Long roleId);

    void updateRole(Long roleId, String newRoleName);

    List<Role> getAllRoles();

    Role findById(Long roleId);

    Role findByName(String roleName);
}
