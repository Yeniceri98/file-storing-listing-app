package com.demo.dao;

import com.demo.entity.FileInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FileInfoRepository extends JpaRepository<FileInfo, Long> {

    Optional<FileInfo> findByFileNameAndFileSize(String fileName, long fileSize);
}
