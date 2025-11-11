package kr.com.mfa.mfaphase1api.service.serviceimpl;

import io.minio.*;
import jakarta.annotation.PostConstruct;
import kr.com.mfa.mfaphase1api.exception.InternalException;
import kr.com.mfa.mfaphase1api.exception.NotFoundException;
import kr.com.mfa.mfaphase1api.model.entity.FileMetadata;
import kr.com.mfa.mfaphase1api.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.InputStream;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileServiceImpl implements FileService {

    @Value("${minio.bucket-name}")
    private String bucketName;

    private final MinioClient minioClient;

    private static final String PREVIEW_PATH = "/api/v1/files/preview/{file-name}";

    @PostConstruct
    public void initBucket() {
        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucketName).build()
            );
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                log.info("Created MinIO bucket '{}'", bucketName);
            }
        } catch (Exception e) {
            throw new InternalException("Failed to initialize MinIO bucket: " + bucketName);
        }
    }

    @Override
    public FileMetadata uploadFile(final MultipartFile file) {
        final String originalName = Optional.ofNullable(file.getOriginalFilename()).orElse("file");
        final String ext = Optional.ofNullable(StringUtils.getFilenameExtension(originalName))
                .map(String::toLowerCase)
                .orElse(null);
        final String objectName = (ext == null) ? UUID.randomUUID().toString()
                : UUID.randomUUID() + "." + ext;

        final String contentType = Optional.ofNullable(file.getContentType())
                .filter(s -> !s.isBlank())
                .orElse("application/octet-stream");

        try (InputStream in = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .contentType(contentType)
                            .stream(in, file.getSize(), -1)
                            .build()
            );
        } catch (Exception e) {
            throw new InternalException("Failed to upload file: " + originalName);
        }

        final String fileUrl = buildPreviewUrl(objectName);

        return FileMetadata.builder()
                .fileName(objectName)
                .fileUrl(fileUrl)
                .fileType(contentType)
                .fileSize(file.getSize())
                .build();
    }

    @Override
    public InputStream getFileByFileName(final String fileName) {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .build()
            );

            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .build()
            );

        } catch (Exception e) {
            throw new NotFoundException("File not found: " + fileName);
        }
    }

    @Override
    public List<FileMetadata> multipleUploadFile(final List<MultipartFile> files) {

        final List<FileMetadata> results = new ArrayList<>(files.size());

        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }
            results.add(uploadFile(file));
        }

        return Collections.unmodifiableList(results);
    }

    @Override
    public void deleteFileByFileName(final String fileName) {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .build()
            );

            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .build()
            );
        } catch (Exception e) {
            throw new NotFoundException("File not found: " + fileName);
        }
    }

    private String buildPreviewUrl(String objectName) {
        return UriComponentsBuilder.fromPath(PREVIEW_PATH)
                .buildAndExpand(objectName)
                .toUriString();
    }

}
