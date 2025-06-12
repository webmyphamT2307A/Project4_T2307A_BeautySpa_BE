package org.aptech.backendmypham.controllers;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@RestController
public class ImageController {

    private static final String UPLOAD_DIR = "uploads";

    @GetMapping("/uploads/{filename}")
    @Operation(summary = "Upload ảnh lên")
    public ResponseEntity<Resource> getImage(@PathVariable String filename) {
        // Xác định đường dẫn tệp từ tên tệp truyền vào
        Path filePath = Paths.get(UPLOAD_DIR).resolve(filename).normalize();
        Resource resource = new FileSystemResource(filePath);

        // Kiểm tra nếu tệp không tồn tại
        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        // Xác định loại MIME cho file (JPEG ở đây)
        String contentType = "image/jpeg"; // Cần thay đổi cho loại ảnh thực tế

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
    @PostMapping(value = "/api/v1/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is empty");
        }
        try {
            File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists()) uploadDir.mkdirs();

            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            File dest = new File(uploadDir, fileName);
            file.transferTo(dest);

            Map<String, String> result = new HashMap<>();
            result.put("url", "/uploads/" + fileName);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace(); // In log lỗi ra console để dễ debug
            return ResponseEntity.status(500).body("Upload failed: " + e.getMessage());
        }
    }
};

