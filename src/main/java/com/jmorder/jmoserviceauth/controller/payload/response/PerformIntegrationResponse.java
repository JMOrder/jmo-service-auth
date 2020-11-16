package com.jmorder.jmoserviceauth.controller.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PerformIntegrationResponse {
    private String token;
    private ConnectedUser connectedUser;
}
