package com.jmorder.jmoserviceauth.controller.payload.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class CheckIntegrationResponse {
    private boolean integratableUserFound;
}
