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
@Document(collection = "visit_sessions")
public class VisitSession {

    @Id
    private ObjectId id;

    @Indexed(unique = true)
    private String sessionId;

    private LocalDateTime firstVisit;

    private LocalDateTime lastActivity;

    private boolean active;
}
