package com.demo.controller;

import com.demo.entity.FileInfo;
import com.demo.exceptions.FileExtensionException;
import com.demo.exceptions.FileSizeException;
import com.demo.service.FileInfoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "FileInfo Controller", description = "CRUD Operations")
@RestController
@RequestMapping("/api/files")
public class FileInfoController {

    private FileInfoService fileInfoService;

    @Autowired
    public FileInfoController(FileInfoService fileInfoService) {
        this.fileInfoService = fileInfoService;
    }

    @Tag(name = "Uploading a File")
    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            fileInfoService.uploadFile(file);
            return ResponseEntity.ok().build();
        } catch (FileExtensionException e) {
            return ResponseEntity.badRequest().build();
        } catch (FileSizeException e) {
            return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @Tag(name = "Listing All Files")
    @GetMapping("/list")
    public ResponseEntity<List<FileInfo>> listFiles() {
        return ResponseEntity.ok(fileInfoService.listFiles());
    }

    @Tag(name = "Get a Single File")
    @GetMapping("/list/{fileId}")
    public ResponseEntity<FileInfo> getSingleFile(@PathVariable("fileId") Long fileId) {
        return ResponseEntity.ok(fileInfoService.getSingleFile(fileId));
    }

    @Tag(name = "Get a File Content")
    @GetMapping("/content/{fileId}")
    public ResponseEntity<byte[]> getFileContent(@PathVariable("fileId") Long fileId) {
        byte[] fileContent = fileInfoService.getFileContent(fileId);

        return ResponseEntity.ok(fileContent);
    }

    @Tag(name = "Updating a File")
    @PutMapping("/update/{fileId}")
    public ResponseEntity<String> updateFile(
            @PathVariable("fileId") Long fileId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("fileName") String fileName,
            @RequestParam("fileExtension") String fileExtension
    ) {
        try {
            fileInfoService.updateFile(fileId, file, fileName, fileExtension);
            return ResponseEntity.ok().build();
        } catch (FileExtensionException e) {
            return ResponseEntity.badRequest().build();
        } catch (FileSizeException e) {
            return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @Tag(name = "Deleting a File")
    @DeleteMapping("/delete/{fileId}")
    public ResponseEntity<String> deleteFile(@PathVariable("fileId") Long fileId) {
        try {
            FileInfo fileInfo = fileInfoService.getSingleFile(fileId);

            if (fileInfo == null) {
                return ResponseEntity.notFound().build();
            }

            fileInfoService.deleteFile(fileId);

            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
