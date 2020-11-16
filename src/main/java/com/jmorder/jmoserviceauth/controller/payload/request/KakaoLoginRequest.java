package com.jmorder.jmoserviceauth.controller.payload.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class KakaoLoginRequest {
    @NotBlank
    @NotNull
    private String code;

//    @NotBlank
    @NotNull
    private String redirectUri;
}
