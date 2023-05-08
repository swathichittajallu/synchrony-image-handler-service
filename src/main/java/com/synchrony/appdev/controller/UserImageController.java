package com.synchrony.appdev.controller;

import com.synchrony.appdev.model.Data;
import com.synchrony.appdev.model.Gallery;
import com.synchrony.appdev.model.Image;
import com.synchrony.appdev.repo.ImageRepository;
import com.synchrony.appdev.service.KafkaProducerService;
import com.synchrony.appdev.util.ImageUtil;
import io.swagger.v3.oas.annotations.Hidden;
import okhttp3.*;

import okhttp3.RequestBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
public class UserImageController {
    @Value("${imgur.image.upload.clientId}")
    private String IMGUR_CLIENT_ID;
    @Value("${imgur.image.upload.url}")
    private String IMGUR_IMAGE_URL;
    @Autowired
    ImageRepository imageRepository;
    @Autowired
    KafkaProducerService producerService;
    Logger logger = LoggerFactory.getLogger(UserImageController.class);

    private static final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/*");
    @GetMapping("/images/{imageId}")
    @PreAuthorize("hasAuthority('admin:read')")
    public Image getImage(@RequestParam("imageID") String id) {
        logger.info("getImage called for id {}", id);
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Client-ID " + IMGUR_CLIENT_ID);
        HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);
        ResponseEntity<Gallery> response = restTemplate.exchange(IMGUR_IMAGE_URL, HttpMethod.GET, entity, Gallery.class);
        List<Data> datas = response.getBody().getData().stream().filter(data -> !(data.getImages() == null))
                .collect(Collectors.toList());
        Optional<Data> responseImage = datas.stream()
                .filter(inner -> inner.getImages().stream().anyMatch(image -> id.equals(image.id))).findFirst();
        if(responseImage.isPresent())
            return responseImage.get().getImages().get(0);
        else
            return null;
    }
    @PostMapping("/images")
    @PreAuthorize("hasAuthority('admin:create')")
    @Hidden
    public Response uploadImage(@RequestParam(value = "file") MultipartFile multiPartFile)  {
        try {
        logger.info("In upload()");
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("imageName", multiPartFile.getOriginalFilename(),
                        RequestBody.create(MediaType.parse("image/*"), convert(multiPartFile)))
                .build();
        Request request = new Request.Builder()
                .url(IMGUR_IMAGE_URL)
                .method("POST", requestBody)
                .header("Authorization", "Client-ID " + IMGUR_CLIENT_ID)
                .build();
        Response response = client.newCall(request).execute();
        logger.debug("Response is msg:" + response.message() + "code:"+ response.code() +
                "isSuccessful:"+response.isSuccessful());
        imageRepository.save(Image.builder()
                    .name(multiPartFile.getOriginalFilename())
                    .type(multiPartFile.getContentType())
                    .image(ImageUtil.compressImage(multiPartFile.getBytes())).build());
        sendMessageToKafkaTopic("Image name: "+ multiPartFile.getOriginalFilename());
            return response;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
    public void sendMessageToKafkaTopic(String message)
    {
        this.producerService.sendMessage(message);
    }
    public static byte[] convertToBytes(File file) throws IOException {
        FileInputStream fl = new FileInputStream(file);
        byte[] arr = new byte[(int)file.length()];
        fl.read(arr);
        fl.close();
        return arr;
    }
    public File convert(MultipartFile file) throws IOException {
        logger.debug("converting given multipartfile to file");
        File convFile = new File(file.getOriginalFilename());
        convFile.createNewFile();
        FileOutputStream fos = new FileOutputStream(convFile);
        fos.write(file.getBytes());
        fos.close();
        return convFile;
    }

    @DeleteMapping("/images/{imageId}")
    @PreAuthorize("hasAuthority('admin:delete')")
    @Hidden
    public Response delete(@RequestParam("imageId") String imageId) {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("text/plain");
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON, "{}");
        Request request = new Request.Builder()
                .url("https://api.imgur.com/3/image/{{imageDeleteHash}}")
                .method("DELETE", body)
                .addHeader("Authorization", "Client-ID {{clientId}}")
                .build();
        Response response = null;
        try {
            response = client.newCall(request).execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return response;
    }
}


