package com.jmorder.jmoserviceauth.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.security.SecureRandom;
import java.util.Date;

@Document
@Data
public class VerificationOTP {
    private static final int DEFAULT_EXPIRY_DURATION_IN_SECONDS = 5 * 60;

    @Id
    private String id;

    @NotBlank
    private String phone;

    private String linkableUserId;

    @NotBlank
    @NotNull
    private String otp;

    @Indexed(expireAfterSeconds = DEFAULT_EXPIRY_DURATION_IN_SECONDS)
    private Date createdAt;

    public static int getDefaultExpiryDurationInSeconds() {
        return DEFAULT_EXPIRY_DURATION_IN_SECONDS;
    }

    public VerificationOTP(String phone, String linkableUserId) {
        this.phone = phone;
        this.linkableUserId = linkableUserId;
        this.otp = this.generateOTP();
        this.createdAt = new Date();
    }

    private String generateOTP() {
        SecureRandom secureRandom = new SecureRandom();
        int randInt = secureRandom.nextInt(100000);
        return String.format("%05d", randInt);
    }

}
