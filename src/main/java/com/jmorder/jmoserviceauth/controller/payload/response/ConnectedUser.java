package com.jmorder.jmoserviceauth.controller.payload.response;

import lombok.Data;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Date;

@Data
public class ConnectedUser {
    private String id;
    private String email;
    private String phone;
    private Date createdAt;
    private Collection<? extends GrantedAuthority> authorities;
}
