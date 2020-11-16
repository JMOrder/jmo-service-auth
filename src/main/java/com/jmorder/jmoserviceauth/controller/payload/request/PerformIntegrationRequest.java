package com.jmorder.jmoserviceauth.controller.payload.request;

import com.jmorder.jmoserviceauth.model.AuthDetail;
import lombok.Data;

@Data
public class PerformIntegrationRequest {
    String phone;
    String authDetail;
}
