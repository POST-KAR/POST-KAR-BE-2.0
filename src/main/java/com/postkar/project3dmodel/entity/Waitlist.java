package com.postkar.project3dmodel.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "waitlist")
public class Waitlist {

    @Id
    private ObjectId id;

    private String name;

    @Indexed(unique = true)
    private String email;

    @Indexed(unique = true)
    private String phone;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
