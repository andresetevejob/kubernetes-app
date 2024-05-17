package com.nextu.kubernetesapp.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "notes")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Todo {
    @Id
    private String id;
    private String description;
    private String fileId;

    @Override
    public String toString() {
        return description;
    }
}
