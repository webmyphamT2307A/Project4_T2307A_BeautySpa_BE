package org.aptech.backendmypham.controllers;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;
import java.nio.file.Paths;

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
};

