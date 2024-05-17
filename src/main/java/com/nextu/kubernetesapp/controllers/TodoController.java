package com.nextu.kubernetesapp.controllers;

import com.nextu.kubernetesapp.entities.Todo;
import com.nextu.kubernetesapp.repository.TodoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class TodoController {
    private final TodoRepository todoRepository;
    @GetMapping(value = "/todos",produces = { "application/json", "application/xml" })
    public ResponseEntity<List<Todo>> find(){
        return ResponseEntity.ok(todoRepository.findAll());
    }
    @PostMapping("/todo")
    public Todo saveTodo(@RequestParam("image") MultipartFile file,
                         @RequestParam String description) throws IOException {
        final Todo todo = new Todo();
        todo.setDescription(description);
        todoRepository.save(todo);
        return todo;
    }
}
