package com.auth.users_service.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;
import java.util.ArrayList;

@Document(collection = "roles")
@lombok.Data
@lombok.NoArgsConstructor
public class Role {

    @Id
    private String id;
    private String name;

    @DBRef
    private List<Permission> permissions = new ArrayList<>();

    public Role(String name) {
        this.name = name;
    }
}
