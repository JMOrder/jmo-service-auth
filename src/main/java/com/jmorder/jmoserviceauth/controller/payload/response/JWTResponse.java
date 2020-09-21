package com.jmorder.jmoserviceauth.controller.payload.response;

import com.jmorder.jmoserviceauth.model.User;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class JWTResponse {
    private String token;
    private String type = "Bearer";
    private String id;
    private String email;
}
