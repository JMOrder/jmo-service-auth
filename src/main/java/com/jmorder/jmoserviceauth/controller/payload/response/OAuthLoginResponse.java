package com.jmorder.jmoserviceauth.controller.payload.response;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.util.Map;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class OAuthLoginResponse extends LoginResponse {
    private Map accessTokenResponse;
}
