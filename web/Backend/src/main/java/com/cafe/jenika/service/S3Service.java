package com.cafe.jenika.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Service
public class S3Service {

    @Autowired
    private S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Value("${aws.s3.region}")
    private String region;

    public String uploadFile(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        
        // Clean original filename of spaces and special characters
        String baseName = "invoice";
        if (originalFilename != null) {
            String cleanName = originalFilename.replaceAll("[^a-zA-Z0-9.-]", "_");
            if (cleanName.contains(".")) {
                baseName = cleanName.substring(0, cleanName.lastIndexOf("."));
            } else {
                baseName = cleanName;
            }
        }
        
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        String folderDate = now.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String fileDateTime = now.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String randomStr = UUID.randomUUID().toString().substring(0, 8);
        
        String fileName = String.format("temp/%s/%s_%s_%s%s", folderDate, baseName, fileDateTime, randomStr, extension);

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        // Return standard S3 public URL
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, fileName);
    }

    public String confirmFile(String tempUrl) {
        if (tempUrl == null || tempUrl.trim().isEmpty() || !tempUrl.contains("/temp/")) {
            return tempUrl;
        }
        try {
            int tempIndex = tempUrl.indexOf("temp/");
            if (tempIndex == -1) {
                return tempUrl;
            }
            String tempKey = tempUrl.substring(tempIndex);
            String targetKey = tempKey.replace("temp/", "invoices/");

            // Copy file from temp/ to invoices/
            software.amazon.awssdk.services.s3.model.CopyObjectRequest copyObjectRequest = 
                    software.amazon.awssdk.services.s3.model.CopyObjectRequest.builder()
                            .sourceBucket(bucketName)
                            .sourceKey(tempKey)
                            .destinationBucket(bucketName)
                            .destinationKey(targetKey)
                            .build();
            s3Client.copyObject(copyObjectRequest);

            // Delete temporary file in temp/
            software.amazon.awssdk.services.s3.model.DeleteObjectRequest deleteObjectRequest = 
                    software.amazon.awssdk.services.s3.model.DeleteObjectRequest.builder()
                            .bucket(bucketName)
                            .key(tempKey)
                            .build();
            s3Client.deleteObject(deleteObjectRequest);

            // Return new URL
            return tempUrl.replace("temp/", "invoices/");
        } catch (Exception e) {
            System.err.println("Lỗi khi xác nhận file S3: " + e.getMessage());
            e.printStackTrace();
            return tempUrl;
        }
    }
}
