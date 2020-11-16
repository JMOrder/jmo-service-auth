package com.jmorder.jmoserviceauth.controller.payload.response;

import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.Map;

@Data
@SuperBuilder
public class OAuthLoginResponse extends LoginResponse {
    private Map accessTokenResponse;
}
