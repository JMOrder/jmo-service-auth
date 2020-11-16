package com.jmorder.jmoserviceauth.controller.payload.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class PerformIntegrationRequest {
    @NotBlank
    String phone;
    @NotBlank
    String authDetail;
}
