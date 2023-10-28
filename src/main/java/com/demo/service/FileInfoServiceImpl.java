package com.demo.service;

import com.demo.dao.FileInfoRepository;
import com.demo.entity.FileInfo;
import com.demo.exceptions.FileExtensionException;
import com.demo.exceptions.FileSizeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@Service
public class FileInfoServiceImpl implements FileInfoService {

    private FileInfoRepository fileInfoRepository;
    Logger LOGGER = LoggerFactory.getLogger(FileInfoServiceImpl.class);

    @Autowired
    public FileInfoServiceImpl(FileInfoRepository fileInfoRepository) {
        this.fileInfoRepository = fileInfoRepository;
    }

    @Override
    public String uploadFile(MultipartFile file) {
        FileInfo fileInfo = new FileInfo();

        try {
            // Get file extension
            String[] parts = file.getOriginalFilename().split("\\.");
            String fileExtension = parts[parts.length - 1];

            fileInfo.setFileName(file.getOriginalFilename());
            fileInfo.setFileExtension(fileExtension);
            fileInfo.setFileSize(file.getSize());

            // Checking if there is same file exist in DB
            Optional<FileInfo> existingFile = fileInfoRepository.findByFileNameAndFileSize(fileInfo.getFileName(), fileInfo.getFileSize());

            if (existingFile.isEmpty()) {
                LOGGER.info("Uploaded file: Name={}, Extension={}, Size={}", file.getOriginalFilename(), fileExtension, file.getSize());

                validateFile(fileInfo);

                // Temp Directory Get and Save
                String tempDirectory = System.getProperty("java.io.tmpdir");
                String filePath = tempDirectory + "/" + file.getOriginalFilename();
                file.transferTo(Paths.get(filePath));

                // Save to DB
                FileInfo fileToSave = new FileInfo();

                fileToSave.setFileName(file.getOriginalFilename());
                fileToSave.setFilePath(filePath);
                fileToSave.setFileExtension(fileExtension.toLowerCase());
                fileToSave.setFileSize(file.getSize());

                fileInfoRepository.save(fileToSave);

            } else {
                LOGGER.info("Attempted to add a file which already exists in DB. You cannot add same file again!");
            }

        } catch (Exception e) {
            LOGGER.info("Error during file upload: {}", e.getMessage(), e);
            return e.getMessage();
        }

        return "File uploaded successfully";
    }

    @Override
    public List<FileInfo> listFiles() {
        LOGGER.info("Listing Files");

        List<FileInfo> existingFiles = fileInfoRepository.findAll();

        for (FileInfo fileInfo : existingFiles) {
            LOGGER.info("File ID: {}, Name: {}, Extension: {}, Size: {}",
                    fileInfo.getId(), fileInfo.getFileName(), fileInfo.getFileExtension(), fileInfo.getFileSize());
        }

        return existingFiles;
    }

    @Override
    public FileInfo getSingleFile(Long fileId) {
        return fileInfoRepository.findById(fileId).orElse(null);
    }

    @Override
    public byte[] getFileContent(Long fileId) {
        FileInfo fileInfo = getSingleFile(fileId);

        if (fileInfo == null) {
            return new byte[0];
        }

        byte[] fileContent;

        try {
            fileContent = Files.readAllBytes(Paths.get(fileInfo.getFilePath()));
        } catch (IOException e) {
            return new byte[0];
        }

        return fileContent;
    }

    @Override
    public String updateFile(Long fileId, MultipartFile file, String fileName, String fileExtension) {
        FileInfo fileInfo = getSingleFile(fileId);

        if (fileInfo == null) {
            return "File not found!";
        }

        try {
            validateFile(fileInfo);

            // Deleting old file from file system
            Files.delete(Paths.get(fileInfo.getFilePath()));

            // Temp Directory Get and Save
            String tempDirectory = System.getProperty("java.io.tmpdir");
            String newFilePath = tempDirectory + "/" + file.getOriginalFilename();
            file.transferTo(Paths.get(newFilePath));

            // Update in DB
            fileInfo.setFileName(fileName);
            fileInfo.setFileExtension(fileExtension);
            fileInfo.setFilePath(newFilePath);
            fileInfo.setFileSize(file.getSize());

            fileInfoRepository.save(fileInfo);

            return "File updated successfully";
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    @Override
    public String deleteFile(Long fileId) {
        FileInfo fileInfo = getSingleFile(fileId);

        if (fileInfo == null) {
            LOGGER.info("File not found with the Id of: " + fileId);
        }

        try {
            // Delete from DB
            LOGGER.info("Deleting File with the Id of: " + fileId);
            fileInfoRepository.deleteById(fileId);

            // Delete from file system
            Files.delete(Paths.get(fileInfo.getFilePath()));
        } catch (IOException e) {
            return "File couldn't be deleted" + e.getMessage();
        }

        return "File deleted successfully";
    }

    private void validateFile(FileInfo fileInfo) throws FileExtensionException, FileSizeException {
        if (!fileInfo.isValidExtension()) {
            throw new FileExtensionException("File extension should one of 'png', 'jpeg', 'jpg', 'docx', 'pdf', 'xlsx'");
        }

        if (!fileInfo.isValidSize()) {
            throw new FileSizeException("File size cannot exceed 5MB");
        }
    }
}
