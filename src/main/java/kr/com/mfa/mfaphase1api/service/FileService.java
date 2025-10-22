package kr.com.mfa.mfaphase1api.service;

import kr.com.mfa.mfaphase1api.model.entity.FileMetadata;
import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;

public interface FileService {

    FileMetadata uploadFile(MultipartFile file);

    InputStream getFileByFileName(String fileName);
}
