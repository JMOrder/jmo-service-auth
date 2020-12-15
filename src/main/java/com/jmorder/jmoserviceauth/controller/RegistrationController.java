package com.jmorder.jmoserviceauth.controller;

import com.jmorder.jmoserviceauth.controller.payload.response.ConnectedUser;
import com.jmorder.jmoserviceauth.controller.payload.response.IntegrationResponse;
import com.jmorder.jmoserviceauth.controller.payload.response.LoginResponse;
import com.jmorder.jmoserviceauth.controller.payload.response.RequestVerificationResponse;
import com.jmorder.jmoserviceauth.model.AuthDetail;
import com.jmorder.jmoserviceauth.model.User;
import com.jmorder.jmoserviceauth.model.VerificationOTP;
import com.jmorder.jmoserviceauth.security.jwt.JWTUtils;
import com.jmorder.jmoserviceauth.service.RefreshTokenService;
import com.jmorder.jmoserviceauth.service.UserService;
import com.jmorder.jmoserviceauth.service.VerificationOTPService;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletResponse;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.time.Duration;
import java.util.Date;
import java.util.Map;

@RestController
@RequestMapping("registration")
@Slf4j
public class RegistrationController {
    @Autowired
    VerificationOTPService verificationOTPService;

    @Autowired
    UserService userService;

    @Autowired
    RefreshTokenService refreshTokenService;

    @Autowired
    JWTUtils jwtUtils;

    @Autowired
    ModelMapper modelMapper;

    @PostMapping
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok().build();
    }

    @PostMapping("verify")
    public ResponseEntity<?> requestVerification(@RequestBody Map<String, String> body) {
        // TODO: JMO-13: Method body will have to send actual request to Twilio with Verification Code (OTP)
        String phone = body.get("phone");
        VerificationOTP verificationOTP = verificationOTPService.createVerificationOTP(phone);

        Date expiresAt = Date.from(verificationOTP.getCreatedAt().toInstant().plus(Duration.ofSeconds(VerificationOTP.getDefaultExpiryDurationInSeconds())));
        var responseBody = RequestVerificationResponse.builder()
                .id(verificationOTP.getId())
                .createdAt(verificationOTP.getCreatedAt())
                .expiresAt(expiresAt)
                .build();

        return ResponseEntity.ok(responseBody);
    }

    @PatchMapping("verify")
    public ResponseEntity<?> performVerification(@RequestBody Map<String, String> body, HttpServletResponse response) {
        // TODO: JMO-13: Method body will have to actually check otp sent via Twilio message
        String id = body.get("id");
        String otp = body.get("otp");
        String authDetailToken = body.get("authDetail");

        if (!verificationOTPService.isOTPMatch(id, otp)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        VerificationOTP verificationOTP = verificationOTPService.loadAndDeleteOTPById(id);

        String linkableUserId = verificationOTP.getLinkableUserId();
        if (linkableUserId == null) {
            return ResponseEntity.noContent().build();
        }

        try {
            AuthDetail authDetail = jwtUtils.getAuthDetailFromJwt(authDetailToken);
            User user = userService.addAuthDetailToUserById(linkableUserId, authDetail);
            String jwt = jwtUtils.generateAuthJwt(user, authDetail.getExpiresAt());
            refreshTokenService.process(response, user);
            LoginResponse loginResponse = LoginResponse.builder()
                .authDetail(authDetailToken)
                .connectedUser(modelMapper.map(user, ConnectedUser.class))
                .token(jwt)
                .build();
            return ResponseEntity.ok(loginResponse);
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }
}
