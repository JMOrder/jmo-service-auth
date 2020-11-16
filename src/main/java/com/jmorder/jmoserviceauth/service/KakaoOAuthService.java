package com.jmorder.jmoserviceauth.service;

import com.jmorder.jmoserviceauth.model.AuthDetail;
import com.jmorder.jmoserviceauth.model.EAuthPlatform;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

@Service
@Slf4j
public class KakaoOAuthService {
    private static final String KAKAO_OAUTH_BASE_URL = "https://kauth.kakao.com";
    private static final String KAKAO_TOKEN_PATH = "/oauth/token";
    private static final String KAKAO_TOKEN_URL = String.format("%s%s", KAKAO_OAUTH_BASE_URL, KAKAO_TOKEN_PATH);
    private static final String KAKAO_API_BASE_URL = "https://kapi.kakao.com";
    private static final String KAKAO_USER_ME_PATH = "/v2/user/me";
    private static final String KAKAO_USER_ME_URL = String.format("%s%s", KAKAO_API_BASE_URL, KAKAO_USER_ME_PATH);
    private static final String KAKAO_LOGOUT_PATH = "/v1/user/logout";
    private static final String KAKAO_LOGOUT_URL = String.format("%s%s", KAKAO_API_BASE_URL, KAKAO_LOGOUT_PATH);
    private static final String GRANT_TYPE = "authorization_code";

    @Value("${jmo.kakao.clientId}")
    private String kakaoServiceClientId;

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    UserService userService;

    public Map getToken(String authCode, String redirectUri) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String uriString = UriComponentsBuilder.fromHttpUrl(KAKAO_TOKEN_URL)
                .queryParam("grant_type", GRANT_TYPE)
                .queryParam("client_id", kakaoServiceClientId)
                .queryParam("code", authCode)
                .queryParam("redirect_uri", redirectUri)
                .toUriString();

        final HttpEntity<Map<String, Object>> entity = new HttpEntity<>(headers);
        var response = restTemplate.postForEntity(uriString, entity, Map.class);
        return response.getBody();
    }

    public AuthDetail getAuthDetail(Map token) {
        String accessToken = (String) token.get("access_token");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        final HttpEntity<Map<String, Object>> entity = new HttpEntity<>(headers);

        var response = restTemplate.postForEntity(KAKAO_USER_ME_URL, entity, Map.class);
        Map body = Objects.requireNonNull(response.getBody());
        String userId = String.valueOf(body.get("id"));
        Date connectedAt = Date.from(Instant.parse((CharSequence) body.get("connected_at")));
        return AuthDetail.builder()
                .platform(EAuthPlatform.KAKAO_OAUTH)
                .userId(userId)
                .connectedAt(connectedAt)
                .expiresAt(new Date(new Date().getTime() + Integer.parseInt(token.get("expires_in").toString())))
                .build();
    }
}
