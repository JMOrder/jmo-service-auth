package com.jmorder.jmoserviceauth.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Transient;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthDetail {
    private EAuthPlatform platform;

    private String userId;

    private Date connectedAt;

    @Transient
    private Date expiresAt;

    @Override
    public String toString() {
        return "AuthDetail{" +
                "platform=" + platform +
                ", userId='" + userId + '\'' +
                ", connectedAt=" + connectedAt +
                ", expiresAt=" + expiresAt +
                '}';
    }
}
