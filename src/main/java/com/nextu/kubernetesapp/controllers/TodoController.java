package com.nextu.kubernetesapp.controllers;

import com.nextu.kubernetesapp.config.TodoProperties;
import com.nextu.kubernetesapp.entities.Todo;
import com.nextu.kubernetesapp.repository.TodoRepository;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.InputStream;
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
        uploadImage(file,description);
        todoRepository.save(todo);
        return todo;
    }
    private void uploadImage(MultipartFile file, String description) throws Exception {
        String fileId = UUID.randomUUID().toString() + "." + file.getOriginalFilename().split("\\.")[1];
       InputStream inputStream = file.getInputStream();
        minioClient.putObject(PutObjectArgs.builder()
                .bucket(properties.getMinioBucket())
                .object(fileId)
                .stream(inputStream, inputStream.available(), -1)
                .build());
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

