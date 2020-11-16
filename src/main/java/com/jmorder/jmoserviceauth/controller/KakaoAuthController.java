package com.jmorder.jmoserviceauth.controller;

import com.jmorder.jmoserviceauth.controller.payload.request.KakaoLoginRequest;
import com.jmorder.jmoserviceauth.controller.payload.response.ConnectedUser;
import com.jmorder.jmoserviceauth.controller.payload.response.OAuthLoginResponse;
import com.jmorder.jmoserviceauth.model.AuthDetail;
import com.jmorder.jmoserviceauth.model.User;
import com.jmorder.jmoserviceauth.security.jwt.JWTUtils;
import com.jmorder.jmoserviceauth.service.KakaoOAuthService;
import com.jmorder.jmoserviceauth.service.RefreshTokenService;
import com.jmorder.jmoserviceauth.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("kakao")
@Slf4j
public class KakaoAuthController {
    @Autowired
    KakaoOAuthService kakaoOAuthService;

    @Autowired
    UserService userService;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    JWTUtils jwtUtils;

    @Autowired
    RefreshTokenService refreshTokenService;

    @PostMapping
    public ResponseEntity<?> authenticateUser(@RequestBody KakaoLoginRequest request, HttpServletResponse response) {
        var token = kakaoOAuthService.getToken(request.getCode(), request.getRedirectUri());
        AuthDetail authDetail = kakaoOAuthService.getAuthDetail(token);
        User user = userService.loadUserByAuthDetail(authDetail);
        String tokenJwt;
        String authDetailJwt;
        try {
            authDetailJwt = jwtUtils.generateAuthDetailJwt(authDetail);
            if (user == null) {
                OAuthLoginResponse loginResponse = OAuthLoginResponse.builder()
                        .accessTokenResponse(token)
                        .authDetail(authDetailJwt)
                        .build();
                return ResponseEntity.accepted().body(loginResponse);
            }

            tokenJwt = jwtUtils.generateAuthJwt(user, Integer.parseInt(token.get("expires_in").toString()));
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        OAuthLoginResponse loginResponse = OAuthLoginResponse.builder()
                .token(tokenJwt)
                .accessTokenResponse(token)
                .connectedUser(modelMapper.map(user, ConnectedUser.class))
                .authDetail(authDetailJwt)
                .build();

        refreshTokenService.process(response, user);
        return ResponseEntity.ok(loginResponse);
    }
}
