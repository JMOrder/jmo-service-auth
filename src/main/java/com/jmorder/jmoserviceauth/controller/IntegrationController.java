package com.jmorder.jmoserviceauth.controller;

import com.jmorder.jmoserviceauth.controller.payload.request.PerformIntegrationRequest;
import com.jmorder.jmoserviceauth.controller.payload.response.CheckIntegrationResponse;
import com.jmorder.jmoserviceauth.controller.payload.response.ConnectedUser;
import com.jmorder.jmoserviceauth.controller.payload.response.PerformIntegrationResponse;
import com.jmorder.jmoserviceauth.model.AuthDetail;
import com.jmorder.jmoserviceauth.model.User;
import com.jmorder.jmoserviceauth.security.jwt.JWTUtils;
import com.jmorder.jmoserviceauth.service.RefreshTokenService;
import com.jmorder.jmoserviceauth.service.UserService;
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
import java.util.Map;

@RestController
@RequestMapping("/integration")
@Slf4j
public class IntegrationController {
    @Autowired
    UserService userService;

    @Autowired
    JWTUtils jwtUtils;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    RefreshTokenService refreshTokenService;

    @PostMapping
    public ResponseEntity<?> checkIntegration(@RequestBody Map<String, String> body) {
        if (!userService.existsUserByUsername(body.get("phone"))) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.noContent().build();
    }

    @PatchMapping
    public ResponseEntity<?> performIntegration(@RequestBody PerformIntegrationRequest requestBody, HttpServletResponse response) {
        try {
            AuthDetail authDetail = jwtUtils.getAuthDetailFromJwt(requestBody.getAuthDetail());
            User user = userService.addAuthDetailToUser(requestBody.getPhone(), authDetail);
            String jwt = jwtUtils.generateAuthJwt(user, authDetail.getExpiresAt());
            refreshTokenService.process(response, user);
            return ResponseEntity.ok(new PerformIntegrationResponse(jwt, modelMapper.map(user, ConnectedUser.class)));
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}
