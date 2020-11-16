package com.jmorder.jmoserviceauth.controller.payload.request;

import com.jmorder.jmoserviceauth.config.pubsub.envelop.UserMessage;
import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class RegistrationRequest {
    @NotBlank
    private String email;

    @NotBlank
    @Size(min = 8, max = 32)
    @ToString.Exclude
    private String password;

    @NotBlank
    @Size(min = 9, max = 15)
    private String phone;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;
}
