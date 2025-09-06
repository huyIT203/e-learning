package com.elearning.elearning_backend.Service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;

@Service
public class S3Service {
    private final S3Client s3Client;
    private final String bucketName = "elearning-uploads-ap-southeast-1";
    
    @Value("${app.aws.cloudfront.domain:}")
    private String cloudfrontDomain;

    public S3Service(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public String uploadFile(MultipartFile file) throws IOException {
        String keyName = file.getOriginalFilename();
        String contentType = file.getContentType();

        // Upload to S3
        s3Client.putObject(PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(keyName)
                        .contentType(contentType) // Thêm content type
                        .build(),
                software.amazon.awssdk.core.sync.RequestBody.fromBytes(file.getBytes())
        );

        // Sử dụng CloudFront URL nếu có, không thì dùng S3 URL
        if (cloudfrontDomain != null && !cloudfrontDomain.isEmpty()) {
            return "https://" + cloudfrontDomain + "/" + keyName;
        } else {
            return "https://" + bucketName + ".s3.amazonaws.com/" + keyName;
        }
    }
}
