package org.aptech.backendmypham.services.serviceImpl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.aptech.backendmypham.models.Role;
import org.aptech.backendmypham.repositories.RoleRepository;
import org.aptech.backendmypham.repositories.UserRepository;
import org.aptech.backendmypham.services.RoleService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    @Override
    public void createRole(String roleName) {
        // Kiểm tra xem role đã tồn tại chưa
        String rolePresent = roleRepository.findAll()
                .stream()
                .filter(role -> role.getName().equals(roleName))
                .map(Role::getName)
                .findFirst()
                .orElse(null);
        if (rolePresent == null) {
            roleRepository.save(new Role(roleName));
        } else {
            // Nếu đã tồn tại thì không tạo mới
            throw new RuntimeException("Role đã tồn tại!");
        }
    }

    @Transactional
    @Override
    public void deleteRole(Long roleId) {
        try {
            // Kiểm tra xem role có tồn tại không
            Optional<Role> role = roleRepository.findById(roleId);
            if (role.isEmpty()) {
                // Nếu không tồn tại thì trả ra lỗi
                throw new RuntimeException("Role đã bị xóa hoặc không tồn tại!");
            }
            //tìm tất cả user có role này và dừng họat động
            userRepository.findAll()
                    .stream()
                    .filter(user -> user.getRole().getId().equals(roleId))
                    .forEach(user -> {
                        user.setIsActive(0);
                        userRepository.save(user);
                    });
            roleRepository.disableRoleById(roleId);
        } catch (Exception e) {
            // Nếu có lỗi xảy ra thì rollback
            throw new RuntimeException("Có lỗi xảy ra khi xóa role: " + e.getMessage());
        }
    }

    @Override
    public void updateRole(Long roleId, String newRoleName) {
        Optional<Role> role = roleRepository.findById(roleId);
        if (role.isPresent()) {
            Role newRole = role.get();
            newRole.setName(newRoleName);
            roleRepository.save(newRole);
        } else {
            // Không tìm thấy thì trả ra lỗi
            throw new RuntimeException("Không tìm thấy role!");
        }
    }

    @Override
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    @Override
    public Role findById(Long roleId) {
        return roleRepository.findById(roleId).orElse(null);

    }

    @Override
    public Role findByName(String roleName) {
        return roleRepository.findAll()
                .stream()
                .filter(role -> role.getName().equals(roleName))
                .findFirst()
                .orElse(null);
    }
}
