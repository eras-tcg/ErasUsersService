package com.auth.users_service.dto;

import java.util.List;

@lombok.Data
@lombok.NoArgsConstructor
public class CheckAccessRequest {
    private String role;
    private List<String> permissions;
}
