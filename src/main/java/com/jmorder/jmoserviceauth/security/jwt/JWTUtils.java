package com.jmorder.jmoserviceauth.security.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.jmorder.jmoserviceauth.model.AuthDetail;
import com.jmorder.jmoserviceauth.model.EAuthPlatform;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

@Component
public class JWTUtils {
    private static final String ROLE_CLAIM_NAME = "ath";
    private static final String PLATFORM_CLAIM_NAME = "pcn";
    private static final String CONNECTED_AT_CLAIM_NAME = "ccn";

    @Value("${jmo.auth.jwt.rsa-private-key}")
    private String rsaPrivateKeyValue;
    @Value("${jmo.auth.jwt.rsa-public-key}")
    private String rsaPublicKeyValue;
    @Value("${jmo.auth.jwt.duration}")
    private int DEFAULT_DURATION;

    public String generateAuthJwt(UserDetails userDetails) throws InvalidKeySpecException, NoSuchAlgorithmException {
        return this.generateAuthJwt(userDetails, DEFAULT_DURATION);
    }

    public String generateAuthJwt(UserDetails userDetails, int duration) throws InvalidKeySpecException, NoSuchAlgorithmException {
        String[] authorities = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).toArray(String[]::new);
        return JWT.create()
                .withJWTId(UUID.randomUUID().toString())
                .withSubject(userDetails.getUsername())
                .withIssuer(JWTConstants.TOKEN_ISSUER)
                .withAudience(JWTConstants.TOKEN_AUDIENCE)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(new Date().getTime() + duration))
                .withArrayClaim(ROLE_CLAIM_NAME, authorities)
                .sign(Algorithm.RSA256(loadPublicKey(), loadPrivateKey()));
    }

    public String generateAuthJwt(UserDetails userDetails, Date expiresAt) throws InvalidKeySpecException, NoSuchAlgorithmException {
        String[] authorities = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).toArray(String[]::new);
        return JWT.create()
                .withJWTId(UUID.randomUUID().toString())
                .withSubject(userDetails.getUsername())
                .withIssuer(JWTConstants.TOKEN_ISSUER)
                .withAudience(JWTConstants.TOKEN_AUDIENCE)
                .withIssuedAt(new Date())
                .withExpiresAt(expiresAt)
                .withArrayClaim(ROLE_CLAIM_NAME, authorities)
                .sign(Algorithm.RSA256(loadPublicKey(), loadPrivateKey()));
    }

    public String generateAuthDetailJwt(AuthDetail authDetail) throws InvalidKeySpecException, NoSuchAlgorithmException {
        return JWT.create()
                .withJWTId(UUID.randomUUID().toString())
                .withSubject(authDetail.getUserId())
                .withIssuer(JWTConstants.TOKEN_ISSUER)
                .withAudience(JWTConstants.TOKEN_AUDIENCE)
                .withIssuedAt(new Date())
                .withExpiresAt(authDetail.getExpiresAt())
                .withClaim(PLATFORM_CLAIM_NAME, authDetail.getPlatform().name())
                .withClaim(CONNECTED_AT_CLAIM_NAME, authDetail.getConnectedAt())
                .sign(Algorithm.RSA256(loadPublicKey(), loadPrivateKey()));
    }

    public String generatePhoneJwt(String phone) throws InvalidKeySpecException, NoSuchAlgorithmException {
        return JWT.create()
                .withJWTId(UUID.randomUUID().toString())
                .withSubject(phone)
                .withIssuer(JWTConstants.TOKEN_ISSUER)
                .withAudience(JWTConstants.TOKEN_AUDIENCE)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(new Date().getTime() + DEFAULT_DURATION))
                .sign(Algorithm.RSA256(loadPublicKey(), loadPrivateKey()));
    }

    public String getUsernameFromJwt(String token) throws InvalidKeySpecException, NoSuchAlgorithmException {
        return JWT.require(Algorithm.RSA256(loadPublicKey(), loadPrivateKey())).build().verify(token).getSubject();
    }

    public AuthDetail getAuthDetailFromJwt(String token) throws InvalidKeySpecException, NoSuchAlgorithmException {
        DecodedJWT decodedJWT = JWT.require(Algorithm.RSA256(loadPublicKey(), loadPrivateKey())).build().verify(token);
        return AuthDetail.builder()
                .userId(decodedJWT.getSubject())
                .platform(decodedJWT.getClaim(PLATFORM_CLAIM_NAME).as(EAuthPlatform.class))
                .connectedAt(decodedJWT.getClaim(CONNECTED_AT_CLAIM_NAME).asDate())
                .expiresAt(decodedJWT.getExpiresAt())
                .build();
    }

    public String parseJwtFrom(String header) {
        if (StringUtils.hasText(header) && header.startsWith(JWTConstants.TOKEN_PREFIX)) {
            return header.replace(JWTConstants.TOKEN_PREFIX, "");
        }
        return null;
    }

    private RSAPublicKey loadPublicKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
        String publicKeyString = rsaPublicKeyValue;
        publicKeyString = publicKeyString
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s+","");
        byte[] x590EncodedBytes = Base64.getDecoder().decode(publicKeyString);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(x590EncodedBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return (RSAPublicKey) keyFactory.generatePublic(keySpec);
    }

    private RSAPrivateKey loadPrivateKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
        String privateKeyString = rsaPrivateKeyValue;
        privateKeyString = privateKeyString
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s+","");
        byte[] pkcs8EncodedBytes = Base64.getDecoder().decode(privateKeyString);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(pkcs8EncodedBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
    }
}
