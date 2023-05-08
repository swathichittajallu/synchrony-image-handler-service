package com.synchrony.appdev.service;

import com.synchrony.appdev.model.Image;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ImageService {
    boolean uploadImage(MultipartFile file) throws IOException;
    Image viewImage(String email);
    boolean deleteImageById(Long id);
    ResponseEntity<byte[]> getImage(String name);
    List<Image> getImages();
}
