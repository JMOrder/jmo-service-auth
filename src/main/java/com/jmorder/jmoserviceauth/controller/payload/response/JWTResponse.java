package com.jmorder.jmoserviceauth.controller.payload.response;

import com.jmorder.jmoserviceauth.model.User;
import lombok.Data;

import java.util.List;

@Data
public class JWTResponse {
    private String token;
    private String type = "Bearer";
    private String id;
    private String email;
    private List<String> roles;

    public JWTResponse(String token, String id, String email, List<String> roles, User user) {
        this.token = token;
        this.id = id;
        this.email = email;
        this.roles = roles;
    }
}
