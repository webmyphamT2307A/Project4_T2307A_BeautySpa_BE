package org.aptech.backendmypham.controllers;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.aptech.backendmypham.dto.ResponseObject;
import org.aptech.backendmypham.enums.Status;
import org.aptech.backendmypham.models.Branch;
import org.aptech.backendmypham.models.Role;
import org.aptech.backendmypham.services.BranchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/branch")
@RequiredArgsConstructor
public class BranchController {
    private final BranchService branchService;

    @GetMapping("")
    @Operation(summary = "Lấy hết branch")
    public ResponseEntity<ResponseObject> getALlBranches() {
        return ResponseEntity.ok(
                new ResponseObject(Status.SUCCESS,"Lấy thành công",branchService.getALlBranch())
        );
    }

    @PostMapping("/create")
    @Operation(summary = "Tạo branch")
    public ResponseEntity<ResponseObject> createBranch(@RequestBody Branch branch) {
        try{
            Branch createdBranch = branchService.createBranch(branch);
            return ResponseEntity.ok(new ResponseObject(Status.SUCCESS, "Tạo chi nhánh thành công", createdBranch));
        }catch (Exception e) {
            return ResponseEntity.badRequest().body(new ResponseObject(Status.ERROR, "Lỗi khi tạo chi nhánh", null));
        }
    }
    @GetMapping("/findByName")
    @Operation(summary = "Lấy branch theo tên")
    public ResponseEntity<ResponseObject> findByName(@RequestParam String branchName) {
        Branch branch = branchService.findByName(branchName);
        if (branch != null) {
            return ResponseEntity.ok(
                    new ResponseObject(Status.SUCCESS, "Thành công", branch)
            );
        } else {
            return ResponseEntity.ok(
                    new ResponseObject(Status.ERROR, "Không tìm thấy branch", null)
            );
        }
    }
    @GetMapping("/findById")
    @Operation(summary = "Lấy branch theo id")
    public ResponseEntity<ResponseObject> findById(@RequestParam Long BiD) {
        Branch branch = branchService.findByID(BiD);
        if (branch != null) {
            return ResponseEntity.ok(
                    new ResponseObject(Status.SUCCESS, "Thành công", branch)
            );
        } else {
            return ResponseEntity.ok(
                    new ResponseObject(Status.ERROR, "Không tìm thấy branch", null)
            );
        }
    }
}
