package com.nextu.kubernetesapp.repository;

import com.nextu.kubernetesapp.entities.Todo;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TodoRepository extends MongoRepository<Todo, String> {
}
