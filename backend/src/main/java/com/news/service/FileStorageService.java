package com.news.service;

import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface FileStorageService {
    List<String> storeFiles(List<MultipartFile> files);
    String storeFile(MultipartFile file);
    String storeAvatar(Long userId, MultipartFile file);
}
