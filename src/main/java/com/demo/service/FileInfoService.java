package com.demo.service;

import com.demo.entity.FileInfo;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileInfoService {

    String uploadFile(MultipartFile file) throws Exception;

    List<FileInfo> listFiles();

    FileInfo getSingleFile(Long fileId);

    byte[] getFileContent(Long fileId);

    String updateFile(Long fileId, MultipartFile file, String fileName, String fileExtension) throws Exception;

    String deleteFile(Long fileId);

}
