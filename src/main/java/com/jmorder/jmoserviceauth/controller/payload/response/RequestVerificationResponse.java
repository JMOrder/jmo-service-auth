package com.jmorder.jmoserviceauth.controller.payload.response;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class RequestVerificationResponse {
    private String id;
    private final Date expiresAt;
    private final String timeunit = "s";
    private final Date createdAt;
}
