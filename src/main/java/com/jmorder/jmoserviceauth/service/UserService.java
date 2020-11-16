package com.jmorder.jmoserviceauth.service;

import com.jmorder.jmoserviceauth.config.pubsub.PubsubConfig;
import com.jmorder.jmoserviceauth.controller.payload.request.RegistrationRequest;
import com.jmorder.jmoserviceauth.model.AuthDetail;
import com.jmorder.jmoserviceauth.model.ERole;
import com.jmorder.jmoserviceauth.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;

@Service
@Slf4j
public class UserService implements UserDetailsService {
    private static final Class<User> ENTITY_CLASS = User.class;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private PubsubConfig.PubsubOutboundGateway pubsubOutboundGateway;

    public User loadUserByEmail(String email) throws UsernameNotFoundException {
        return mongoTemplate.findOne(Query.query(Criteria.where("email").is(email)), ENTITY_CLASS);
    }

    public User loadUserByPhone(String phone) throws UsernameNotFoundException {
        return mongoTemplate.findOne(Query.query(Criteria.where("phone").is(phone)), ENTITY_CLASS);
    }

    @Override
    public User loadUserByUsername(String phone) throws UsernameNotFoundException {
        return loadUserByPhone(phone);
    }

    public boolean existsUserByEmail(String email) {
        return mongoTemplate.exists(Query.query(Criteria.where("email").is(email)), ENTITY_CLASS);
    }

    public boolean existsUserByPhone(String phone) {
        return mongoTemplate.exists(Query.query(Criteria.where("phone").is(phone)), ENTITY_CLASS);
    }

    public boolean existsUserByUsername(String username) {
        return existsUserByPhone(username);
    }

    public User createUserByRegistrationRequest(RegistrationRequest registrationRequest) {
        User user = User.builder()
                .email(registrationRequest.getEmail())
                .phone(registrationRequest.getPhone())
                .password(passwordEncoder.encode(registrationRequest.getPassword()))
                .isEmailVerified(false)
                .isPhoneVerified(false)
                .authorities(new ArrayList<GrantedAuthority>(Collections.singletonList(new SimpleGrantedAuthority(ERole.ROLE_USER.name()))))
                .build();
        user = mongoTemplate.insert(user);
        pubsubOutboundGateway.sendUserToPubsub(registrationRequest.toMessage());
        return user;
    }

    public boolean existsUserByAuthDetail(AuthDetail authDetail) {
        return mongoTemplate.exists(Query.query(Criteria.where("authDetails").is(authDetail)), ENTITY_CLASS);
    }

    public User loadUserByAuthDetail(AuthDetail authDetail) {
        return mongoTemplate.findOne(Query.query(Criteria.where("authDetails").is(authDetail)), ENTITY_CLASS);
    }

    public User addAuthDetailToUser(String phone, AuthDetail authDetail) {
        User user = loadUserByUsername(phone);
        user.getAuthDetails().add(authDetail);
        return mongoTemplate.save(user);
    }
}
