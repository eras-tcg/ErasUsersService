package com.auth.users_service.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


@Document(collection = "permissions")
@lombok.Data
@lombok.NoArgsConstructor
public class Permission {
    @Id
    private String id;
    private String name;
    private String description;

    public Permission(String name) {
        this.name = name;
    }

    public Permission(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
