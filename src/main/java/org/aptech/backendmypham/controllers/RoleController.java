package org.aptech.backendmypham.controllers;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.aptech.backendmypham.dto.ResponseObject;
import org.aptech.backendmypham.enums.Status;
import org.aptech.backendmypham.models.Role;
import org.aptech.backendmypham.repositories.UserRepository;
import org.aptech.backendmypham.services.RoleService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
public class RoleController {
    private final RoleService roleService;

    @GetMapping("")
    @Operation(summary = "Lấy tất cả role trong hệ thống")
    public ResponseEntity<ResponseObject> getAllRoles() {
        return ResponseEntity.ok(
                new ResponseObject(Status.SUCCESS, "Thành công", roleService.getAllRoles())
        );
    }

    @GetMapping("/findById")
    @Operation(summary = "Lấy role theo id")
    public ResponseEntity<ResponseObject> findById(@RequestParam Long roleId) {
        Role role = roleService.findById(roleId);
        if (role != null) {
            return ResponseEntity.ok(
                    new ResponseObject(Status.SUCCESS, "Thành công", role)
            );
        } else {
            return ResponseEntity.ok(
                    new ResponseObject(Status.ERROR, "Không tìm thấy role", null)
            );
        }
    }

    @GetMapping("/findByName")
    @Operation(summary = "Lấy role theo tên")
    public ResponseEntity<ResponseObject> findByName(@RequestParam String roleName) {
        Role role = roleService.findByName(roleName);
        if (role != null) {
            return ResponseEntity.ok(
                    new ResponseObject(Status.SUCCESS, "Thành công", role)
            );
        } else {
            return ResponseEntity.ok(
                    new ResponseObject(Status.ERROR, "Không tìm thấy role", null)
            );
        }
    }

    @GetMapping("/create")
    @Operation(summary = "Tạo mới role")
    public ResponseEntity<ResponseObject> createRole(@RequestParam String roleName) {
        try {
            roleService.createRole(roleName);
            return ResponseEntity.ok(
                    new ResponseObject(Status.SUCCESS, "Tạo mới role thành công", null)
            );
        } catch (Exception e) {
            return ResponseEntity.ok(
                    new ResponseObject(Status.ERROR, "Tạo mới role thất bại: " + e.getMessage(), null)
            );
        }
    }

    @PutMapping("/update")
    @Operation(summary = "Cập nhật role")
    public ResponseEntity<ResponseObject> updateRole(@RequestParam Long roleId, @RequestParam String newRoleName) {
        try {
            roleService.updateRole(roleId, newRoleName);
            return ResponseEntity.ok(
                    new ResponseObject(Status.SUCCESS, "Cập nhật role thành công", null)
            );
        } catch (Exception e) {
            return ResponseEntity.ok(
                    new ResponseObject(Status.ERROR, "Cập nhật role thất bại: " + e.getMessage(), null)
            );
        }
    }

    @PutMapping("/delete")
    @Operation(summary = "Xóa role")
    public ResponseEntity<ResponseObject> deleteRole(@RequestParam Long roleId) {
        try {
            roleService.deleteRole(roleId);
            return ResponseEntity.ok(
                    new ResponseObject(Status.SUCCESS, "Xóa role thành công", null)
            );
        } catch (Exception e) {
            return ResponseEntity.ok(
                    new ResponseObject(Status.ERROR, "Xóa role thất bại: " + e.getMessage(), null)
            );
        }
    }
}
