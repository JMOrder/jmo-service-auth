package com.jmorder.jmoserviceauth.config.pubsub.envelop;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class UserMessage {
    @NotBlank
    private String email;

    @NotBlank
    private String phone;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;
}
