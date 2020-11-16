package com.jmorder.jmoserviceauth.controller.payload.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class KakaoLoginRequest {
    @NotBlank
    private String code;

    @NotBlank
    private String redirectUri;
}
