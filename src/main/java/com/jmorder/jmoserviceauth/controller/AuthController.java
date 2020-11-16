package com.jmorder.jmoserviceauth.controller;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Objects;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import com.jmorder.jmoserviceauth.controller.payload.request.LoginRequest;
import com.jmorder.jmoserviceauth.controller.payload.request.RegistrationRequest;
import com.jmorder.jmoserviceauth.controller.payload.response.JWTResponse;
import com.jmorder.jmoserviceauth.model.RefreshToken;
import com.jmorder.jmoserviceauth.model.User;
import com.jmorder.jmoserviceauth.security.jwt.JWTConstants;
import com.jmorder.jmoserviceauth.security.jwt.JWTUtils;
import com.jmorder.jmoserviceauth.service.RefreshTokenService;
import com.jmorder.jmoserviceauth.service.UserService;
import com.mongodb.lang.Nullable;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@Slf4j
public class AuthController {
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JWTUtils jwtUtils;
    @Autowired
    private UserService userService;
    @Autowired
    private RefreshTokenService refreshTokenService;

    @HystrixCommand(fallbackMethod = "fallbackLogin")
    @PostMapping
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            User user = (User) authentication.getPrincipal();
            String jwt;
            try {
                jwt = jwtUtils.generateAuthJwt(user);
            } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }

            refreshTokenService.process(response, user);
            JWTResponse jwtResponse = JWTResponse.builder()
                    .token(jwt)
                    .type(JWTConstants.TOKEN_PREFIX.trim())
                    .id(user.getId())
                    .email(user.getEmail())
                    .build();
            return ResponseEntity.ok(jwtResponse);
        } catch (AuthenticationException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
    }

    @HystrixCommand
    @DeleteMapping
    public ResponseEntity<?> logout(@Nullable @CookieValue(value = "refresh_token", required = false) String refreshTokenId, HttpServletResponse response) {
        if (refreshTokenId != null) {
            refreshTokenService.removeById(refreshTokenId);
            Cookie refreshTokenCookie = new Cookie("refresh_token", null);
            refreshTokenCookie.setMaxAge(0); // immediately expire
            refreshTokenCookie.setPath("/");
            response.addCookie(refreshTokenCookie);
        }
        return ResponseEntity.ok().build();
    }

    @HystrixCommand
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegistrationRequest registrationRequest) {
        if (userService.existsUserByUsername(registrationRequest.getPhone())) {
            return ResponseEntity.unprocessableEntity().build();
        }

        userService.createUserByRegistrationRequest(registrationRequest);
        return ResponseEntity.ok().build();
    }

    @HystrixCommand
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@Nullable @CookieValue(value = "refresh_token", required = false) String refreshTokenId,
                                          HttpServletRequest request, HttpServletResponse response) {
        if (refreshTokenId == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        RefreshToken existingToken = refreshTokenService.findById(refreshTokenId);

        String username = Objects.requireNonNull(existingToken).getUsername();
        User user = (User) userService.loadUserByUsername(username);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                user, null, user.getAuthorities());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt;
        try {
            jwt = jwtUtils.generateAuthJwt(user);
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        refreshTokenService.removeById(refreshTokenId);
        refreshTokenService.process(response, user);

        JWTResponse jwtResponse = JWTResponse.builder()
                .token(jwt)
                .type(JWTConstants.TOKEN_PREFIX.trim())
                .id(user.getId())
                .email(user.getEmail())
                .build();

        return ResponseEntity.ok(jwtResponse);
    }

    public ResponseEntity<?> fallbackLogin(@Valid @RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}
