package com.jmorder.jmoserviceauth.controller;

import com.jmorder.jmoserviceauth.controller.payload.request.LoginRequest;
import com.jmorder.jmoserviceauth.controller.payload.request.RegistrationRequest;
import com.jmorder.jmoserviceauth.controller.payload.response.JWTResponse;
import com.jmorder.jmoserviceauth.model.User;
import com.jmorder.jmoserviceauth.model.ERole;
import com.jmorder.jmoserviceauth.model.RefreshToken;
import com.jmorder.jmoserviceauth.security.jwt.JWTUtils;
import com.jmorder.jmoserviceauth.service.UserService;
import com.mongodb.lang.Nullable;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.*;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
public class AuthController {
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JWTUtils jwtUtils;
    @Autowired
    private MongoOperations mongoTemplate;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserService userService;

    @HystrixCommand
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
                jwt = jwtUtils.generateJwtToken(user);
            } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
            List<String> roles = user.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            this.processRefreshToken(response, user.getUsername());
            return ResponseEntity.ok(new JWTResponse(jwt, user.getId(), user.getUsername(), roles, user));
        } catch (AuthenticationException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
    }

    @DeleteMapping
    public ResponseEntity<?> logout(@Nullable @CookieValue(value = "refresh_token") String refreshTokenId, HttpServletResponse response) {
        if (refreshTokenId != null) {
            mongoTemplate.remove(Query.query(Criteria.where("id").is(refreshTokenId)), RefreshToken.class);
            Cookie refreshTokenCookie = new Cookie("refresh_token", null);
            refreshTokenCookie.setMaxAge(0); // immediately expire
            refreshTokenCookie.setPath("/");
            response.addCookie(refreshTokenCookie);
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegistrationRequest registrationRequest) {
        if (mongoTemplate.exists(Query.query(Criteria.where("email").is(registrationRequest.getEmail())), User.class)) {
            return ResponseEntity.unprocessableEntity().build();
        }

        User user = User
                .builder()
                .email(registrationRequest.getEmail())
                .password(passwordEncoder.encode(registrationRequest.getPassword()))
                .authorities(new ArrayList<GrantedAuthority>(Collections.singletonList(new SimpleGrantedAuthority(ERole.ROLE_USER.name()))))
                .build();

        mongoTemplate.insert(user);
//        userKafkaTemplate.send("fs-user-topic", registrationRequest.toMessage());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@CookieValue(value = "refresh_token") String refreshTokenId,
                                          HttpServletRequest request, HttpServletResponse response) {
        if (refreshTokenId != null) {
            RefreshToken existingToken = mongoTemplate.findById(refreshTokenId, RefreshToken.class);

            String username = Objects.requireNonNull(existingToken).getUsername();
            User user = (User) userService.loadUserByUsername(username);
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    user, null, user.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authentication);

            String jwt;
            try {
                jwt = jwtUtils.generateJwtToken(user);
            } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }

            List<String> roles = user.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            mongoTemplate.remove(Query.query(Criteria.where("id").is(refreshTokenId)), RefreshToken.class);
            this.processRefreshToken(response, username);

            return ResponseEntity.ok(new JWTResponse(jwt, user.getId(), user.getUsername(), roles, user));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    private void processRefreshToken(HttpServletResponse response, String jwtId) {
        RefreshToken refreshToken = mongoTemplate.insert(new RefreshToken(jwtId));
        Cookie refreshTokenCookie = new Cookie("refresh_token", refreshToken.getId());
        refreshTokenCookie.setMaxAge(24 * 60 * 60); // expires in 7 days
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setPath("/");
//    refreshTokenCookie.setSecure(true); // TODO: Enable this when deploy to production is ready
        response.addCookie(refreshTokenCookie);
    }

}
