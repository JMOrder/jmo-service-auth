package com.jmorder.jmoserviceauth.controller.payload.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class PerformVerificationRequest {
    @NotBlank
    String id;

    @NotBlank
    String otp;

    @NotBlank
    String authDetail;
}
