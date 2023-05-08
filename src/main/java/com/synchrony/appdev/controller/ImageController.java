package com.synchrony.appdev.controller;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.synchrony.appdev.model.Data;
import com.synchrony.appdev.model.Gallery;
import com.synchrony.appdev.service.ImageService;
import com.synchrony.appdev.model.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;


@CrossOrigin
@RestController
@RequestMapping("/api/v1/images")
public class ImageController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ImageController.class);
    @Value( "${imgur.image.upload.url}" )
    private String IMGUR_URL;
    @Value( "${imgur.image.upload.clientId}" )
    private String IMGUR_CLIENTID;
    @Autowired
    ImageService imageService;
    @PostMapping("/upload")
    public ResponseEntity<String> uploadImage(@RequestParam("image") MultipartFile file)
            throws IOException {
        imageService.uploadImage(file);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new String("Image uploaded," +
                        file.getOriginalFilename()));
    }

    @GetMapping({"/health"})
    public int healthCheck(){
        System.out.println("Running health check");
        return 200;
    }
    @GetMapping({"/health/{imageId}"})
    public Image healthCheckImage(@RequestParam("imageID") String id) {
        LOGGER.info("getImage called for id {}", id);
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Client-ID " + IMGUR_CLIENTID);
        HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);
        ResponseEntity<Gallery> response = restTemplate.exchange(IMGUR_URL, HttpMethod.GET, entity, Gallery.class);
        List<Data> datas = response.getBody().getData().stream().filter(data -> !(data.getImages() == null))
                .collect(Collectors.toList());
        Optional<Data> responseImage = datas.stream()
                .filter(inner -> inner.getImages().stream().anyMatch(image -> id.equals(image.id))).findFirst();
        if(responseImage.isPresent())
            return responseImage.get().getImages().get(0);
        else
            return null;
    }
    @GetMapping(path = {"/info/{imageName}"})
    public Image getImageDetails(@PathVariable("imageName") String imageName) throws IOException {
        return imageService.viewImage(imageName);
    }


    //  @Cacheable("image")
    @GetMapping({"/imageID"})
    public Image getImage(@RequestParam("imageID") String id) {
        LOGGER.info("getImage called for id {}", id);
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Client-ID " + IMGUR_CLIENTID);
        HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);
        ResponseEntity<Gallery> response = restTemplate.exchange(IMGUR_URL, HttpMethod.GET, entity, Gallery.class);
        List<Data> datas = response.getBody().getData().stream().filter(data -> !(data.getImages() == null))
                .collect(Collectors.toList());
        Optional<Data> responseImage = datas.stream()
                .filter(inner -> inner.getImages().stream().anyMatch(image -> id.equals(image.id))).findFirst();
        if(responseImage.isPresent())
            return responseImage.get().getImages().get(0);
        else
            return null;
    }

    @DeleteMapping("/{imageId}")
    public ResponseEntity<HttpStatus> deleteImageByUserId(@PathVariable("id") long id) {
        try {
            imageService.deleteImageById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



}
