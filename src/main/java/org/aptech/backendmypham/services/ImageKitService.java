package org.aptech.backendmypham.services;

import org.springframework.web.multipart.MultipartFile;

public interface ImageKitService {
    public String uploadImage(MultipartFile file) throws Exception;
}
