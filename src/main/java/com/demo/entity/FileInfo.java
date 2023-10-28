package com.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;

@Entity(name = "file_info")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "file_extension")
    private String fileExtension;

    @Column(name = "file_size")
    private long fileSize;

    public static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            "png", "jpeg", "jpg", "docx", "pdf", "xlsx"
    );

    public boolean isValidExtension() {
        return ALLOWED_EXTENSIONS.contains(this.fileExtension.toLowerCase());
    }

    public boolean isValidSize() {
        return this.fileSize <= 5 * 1024 * 1024;
    }
}
