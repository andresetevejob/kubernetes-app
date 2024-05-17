package com.nextu.kubernetesapp.controllers;

import com.nextu.kubernetesapp.config.TodoProperties;
import com.nextu.kubernetesapp.entities.Todo;
import com.nextu.kubernetesapp.exceptions.FileContentException;
import com.nextu.kubernetesapp.repository.TodoRepository;
import com.nextu.kubernetesapp.utils.FileUtils;
import com.nextu.kubernetesapp.utils.MimeTypeUtils;
import io.minio.*;
import io.minio.errors.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@EnableConfigurationProperties(TodoProperties.class)
public class TodoController {
    private final TodoRepository todoRepository;
    private MinioClient minioClient;
    private final TodoProperties properties;
    @PostConstruct
    public void init() throws InterruptedException {
        initMinio();
    }
    @GetMapping(value = "/todos",produces = { "application/json", "application/xml" })
    public ResponseEntity<List<Todo>> find(){
        return ResponseEntity.ok(todoRepository.findAll());
    }

  @PostMapping("/todo")
  public Todo saveTodo(@RequestParam("image") MultipartFile file, @RequestParam String description)
      throws Exception {
        final Todo todo = new Todo();
        todo.setDescription(description);
        String fileId = uploadImage(file,description);
        todo.setFileId(fileId);
        todoRepository.save(todo);
        return todo;
    }
    private String uploadImage(MultipartFile file, String description) throws Exception {
        String fileId = UUID.randomUUID().toString() + "." + file.getOriginalFilename().split("\\.")[1];
        InputStream inputStream = file.getInputStream();
        minioClient.putObject(PutObjectArgs.builder()
                .bucket(properties.getMinioBucket())
                .object(fileId)
                .stream(inputStream, inputStream.available(), -1)
                .build());
        return fileId;
    }

    @GetMapping(value = "todo/{name}")
    public ResponseEntity<?> find(@PathVariable String name) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException, FileContentException {
        InputStream stream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(properties.getMinioBucket())
                        .object(name)
                        .build()
        );
        var extension = FileUtils.getExtension(name);
        return ResponseEntity
                .ok()
                .contentType(MediaType.parseMediaType(MimeTypeUtils.getMimeType(extension)))
                .body(new InputStreamResource(stream));

    }

    private void initMinio() throws InterruptedException {
        boolean success = false;
        while (!success) {
            try {
                minioClient = MinioClient.builder()
                        .endpoint("http://" + properties.getMinioHost() + ":9000")
                        .credentials(properties.getMinioAccessKey(), properties.getMinioSecretKey())
                        .build();
                // Check if the bucket already exists.
                boolean isExist = minioClient.bucketExists(BucketExistsArgs
                        .builder()
                        .bucket(properties.getMinioBucket())
                        .build());
                if (isExist) {
                    System.out.println("> Bucket already exists.");
                } else {
                    minioClient.makeBucket(MakeBucketArgs
                            .builder()
                            .bucket(properties.getMinioBucket())
                            .build());
                }
                success = true;
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("> Minio Reconnect: " + properties.isMinioReconnectEnabled());
                if (properties.isMinioReconnectEnabled()) {
                    Thread.sleep(5000);
                } else {
                    success = true;

                }
            }
        }
        System.out.println("> Minio initialized!");
    }
}

