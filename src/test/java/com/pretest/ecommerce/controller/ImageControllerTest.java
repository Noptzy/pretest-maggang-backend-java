package com.pretest.ecommerce.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ImageController.class)
@TestPropertySource(properties = "noptzy.upload.path=src/test/resources/images")
class ImageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @org.springframework.boot.test.mock.mockito.MockBean
    private com.pretest.ecommerce.service.AuthService authService;

    @Test
    void getImage_Success() throws Exception {
        Path imageDir = Path.of("src/test/resources/images");
        if (!Files.exists(imageDir)) {
            Files.createDirectories(imageDir);
        }
        Path imagePath = imageDir.resolve("test-image.jpg");
        if (!Files.exists(imagePath)) {
            Files.createFile(imagePath);
        }

        try {
            mockMvc.perform(get("/api/images/test-image.jpg"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType("image/jpeg"));
        } finally {
            Files.deleteIfExists(imagePath);
        }
    }

    @Test
    void getImage_NotFound() throws Exception {
        mockMvc.perform(get("/api/images/non-existent-image.jpg"))
                .andExpect(status().isNotFound());
    }
}