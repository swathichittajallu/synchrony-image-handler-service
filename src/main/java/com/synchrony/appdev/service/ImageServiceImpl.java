package com.synchrony.appdev.service;

import com.synchrony.appdev.model.Image;

import com.synchrony.appdev.repo.ImageRepository;
import com.synchrony.appdev.util.ImageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service("imageHandlerService")
public class ImageServiceImpl implements ImageService {

    @Autowired
    ImageRepository imageRepository;
    @Override
    public boolean uploadImage(MultipartFile file) throws IOException {
        imageRepository.save(Image.builder()
                .name(file.getOriginalFilename())
                .type(file.getContentType())
                .image(ImageUtil.compressImage(file.getBytes())).build());
        return true;
    }

    @Override
    public Image viewImage(String imageName) {
        final Optional<Image> dbImage = imageRepository.findByName(imageName);
        return Image.builder()
                .name(dbImage.get().getName())
                .type(dbImage.get().getType())
                .image(ImageUtil.decompressImage(dbImage.get().getImage())).build();
    }

    @Override
    public boolean deleteImageById(Long id) {
        imageRepository.deleteImageById(id);
        return true;
    }

    @Override
    public ResponseEntity<byte[]> getImage(String name) {
        final Optional<Image> dbImage = imageRepository.findByName(name);
        return ResponseEntity
                .ok()
                .contentType(MediaType.valueOf(dbImage.get().getType()))
                .body(ImageUtil.decompressImage(dbImage.get().getImage()));
    }

    @Override
    public List<Image> getImages() {
        return null;
    }
}