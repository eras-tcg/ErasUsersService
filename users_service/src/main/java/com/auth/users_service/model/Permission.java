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

    public Permission(String name) {
        this.name = name;
    }
}
