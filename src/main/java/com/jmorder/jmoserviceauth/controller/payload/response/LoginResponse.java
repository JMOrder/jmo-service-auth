package com.jmorder.jmoserviceauth.controller.payload.response;

import com.jmorder.jmoserviceauth.model.AuthDetail;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
public class LoginResponse {
    private String token;
    private final String type = "Bearer";
    private String authDetail;
    private ConnectedUser connectedUser;
}
