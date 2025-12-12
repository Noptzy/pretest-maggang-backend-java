package com.pretest.ecommerce.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class ImageService {

    @Value("${noptzy.upload.path}")
    private String uploadPath;

    public String saveImage(MultipartFile file) {
        if (file.isEmpty()) throw new RuntimeException("File kosong");

        try {
            Path root = Paths.get(uploadPath);
            if (!Files.exists(root)) Files.createDirectories(root);

            String filename = UUID.randomUUID().toString() + "-" + file.getOriginalFilename();
            Path target = root.resolve(filename);

            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            return ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/api/images/")
                    .path(filename)
                    .toUriString();

        } catch (IOException e) {
            throw new RuntimeException("Gagal upload file: " + e.getMessage());
        }
    }
}