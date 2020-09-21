package com.jmorder.jmoserviceauth.controller.payload.request;

import com.jmorder.jmoserviceauth.messageq.envelop.UserMessage;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class RegistrationRequest {
    @NotBlank
    private String email;

    @NotBlank
    @Size(min = 8, max = 32)
    private String password;

    @NotBlank
    @Size(min = 9, max = 15)
    private String phone;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    public UserMessage toMessage() {
        return UserMessage.builder()
                .email(email)
                .phone(phone)
                .firstName(firstName)
                .lastName(lastName)
                .build();
    }
}
