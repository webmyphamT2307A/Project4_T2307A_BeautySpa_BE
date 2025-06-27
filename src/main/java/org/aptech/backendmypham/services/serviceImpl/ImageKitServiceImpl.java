package org.aptech.backendmypham.services.serviceImpl;

import io.imagekit.sdk.ImageKit;
import io.imagekit.sdk.config.Configuration;
import io.imagekit.sdk.models.FileCreateRequest;
import io.imagekit.sdk.models.results.Result;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.aptech.backendmypham.services.ImageKitService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ImageKitServiceImpl implements ImageKitService {

    @Value("${imagekit.public-key}")
    private String publicKey;

    @Value("${imagekit.private-key}")
    private String privateKey;

    @Value("${imagekit.url-endpoint}")
    private String urlEndpoint;

    @PostConstruct
    public void setup() {
        ImageKit imageKit = ImageKit.getInstance();
        Configuration config = new Configuration(publicKey, privateKey, urlEndpoint);
        imageKit.setConfig(config);
    }

    @Override
    public String uploadImage(MultipartFile file) throws Exception {
        byte[] fileBytes = file.getBytes();
        String fileName = file.getOriginalFilename();

        FileCreateRequest fileCreateRequest = new FileCreateRequest(fileBytes, fileName);
        fileCreateRequest.setUseUniqueFileName(true);

        Result result = ImageKit.getInstance().upload(fileCreateRequest);

        if (result != null && result.getUrl() != null) {
            return result.getUrl();
        }

        throw new RuntimeException("Không thể upload ảnh lên ImageKit.io. Kết quả không hợp lệ.");
    }
}
