package com.jmorder.jmoserviceauth.util;

import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

@Component
@Slf4j
public class FirebaseDynamicLink {
    private static final String API_URL = "https://firebasedynamiclinks.googleapis.com/v1/shortLinks";

    @Value("${jmo.firebase.web-api-key}")
    private String firebaseWebApiKey;

    @Value("${jmo.firebase.dynamic-link.url-prefix}")
    private String firebaseDynamicLinkUrlPrefix;

    @Value("${jmo.firebase.dynamic-link.web-host}")
    private String firebaseDynamicLinkWebHost;

    @Value("${jmo.firebase.dynamic-link.android-package-name}")
    private String firebaseDynamicLinkAndroidPackageName;

    @Value("${jmo.firebase.dynamic-link.ios-bundle-id}")
    private String firebaseDynamicLinkiOSBundleId;

    @Value("${jmo.firebase.dynamic-link.ios-app-store-id}")
    private String firebaseDynamicLinkiOSAppStoreId;


    @Value("${jmo.firebase.dynamic-link.fallback-link}")
    private String firebaseDynamicLinkFallBackLink;

    @Autowired
    RestTemplate restTemplate;

    public String convert(String targetPath) throws MalformedURLException {
        String uriString = UriComponentsBuilder.fromHttpUrl(API_URL)
                .queryParam("key", firebaseWebApiKey)
                .toUriString();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        JsonObject dynamicLinkPayload = this.generateDynamicLinkPayload(targetPath);
        HttpEntity<String> entity = new HttpEntity<>(dynamicLinkPayload.toString(), headers);

        var response = restTemplate.postForEntity(uriString, entity, Map.class);
        Map<String, String> body = response.getBody();
        String shortLink = body.get("shortLink");
        String previewLink = body.get("previewLink");
        log.info("Short Link: " + shortLink);
        log.info("Preview Link: " + previewLink);

        return shortLink;
    }

    private JsonObject generateDynamicLinkPayload(String targetPath) throws MalformedURLException {
        JsonObject output = new JsonObject();

        JsonObject dynamicLinkInfo = new JsonObject();
        dynamicLinkInfo.addProperty("domainUriPrefix", firebaseDynamicLinkUrlPrefix);
        log.info(firebaseDynamicLinkUrlPrefix);
        log.info(firebaseDynamicLinkWebHost);
        log.info(targetPath);
        URL link = new URL(new URL(firebaseDynamicLinkWebHost), targetPath);
        dynamicLinkInfo.addProperty("link", link.toString());

        JsonObject androidInfo = new JsonObject();
        androidInfo.addProperty("androidPackageName", firebaseDynamicLinkAndroidPackageName);
        androidInfo.addProperty("androidFallbackLink", firebaseDynamicLinkFallBackLink);
        dynamicLinkInfo.add("androidInfo", androidInfo);

        JsonObject iosInfo = new JsonObject();
        iosInfo.addProperty("iosBundleId", firebaseDynamicLinkiOSBundleId);
        iosInfo.addProperty("iosFallbackLink", firebaseDynamicLinkFallBackLink);
        iosInfo.addProperty("iosAppStoreId", firebaseDynamicLinkiOSAppStoreId);
        dynamicLinkInfo.add("iosInfo", iosInfo);

//        JsonObject navigationInfo = new JsonObject();
//        navigationInfo.addProperty("enableForcedRedirect", true);
//        dynamicLinkInfo.add("navigationInfo", navigationInfo);

        output.add("dynamicLinkInfo", dynamicLinkInfo);

        JsonObject suffix = new JsonObject();
        suffix.addProperty("option", "SHORT");

        output.add("suffix", suffix);

        return output;
    }
}