package com.jmorder.jmoserviceauth.service;

import java.util.ArrayList;
import java.util.Collections;

import com.jmorder.jmoserviceauth.controller.payload.request.RegistrationRequest;
import com.jmorder.jmoserviceauth.messageq.envelop.UserMessage;
import com.jmorder.jmoserviceauth.model.ERole;
import com.jmorder.jmoserviceauth.model.User;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService implements UserDetailsService {
    private static final Class<User> ENTITY_CLASS = User.class;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    KafkaTemplate<String, UserMessage> userKafkaTemplate;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    NewTopic userTopic;

    public UserDetails loadUserByEmail(String email) throws UsernameNotFoundException {
        return mongoTemplate.findOne(Query.query(Criteria.where("email").is(email)), ENTITY_CLASS);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return loadUserByEmail(username);
    }

    public boolean existsUserByEmail(String email) {
        Query query = Query.query(Criteria.where("email").is(email));
        return mongoTemplate.exists(query, ENTITY_CLASS);
    }

    public UserDetails createUserByRegistrationRequest(RegistrationRequest registrationRequest) {
        User user = User.builder()
                .email(registrationRequest.getEmail())
                .phone(registrationRequest.getPhone())
                .password(passwordEncoder.encode(registrationRequest.getPassword()))
                .isEmailVerified(false)
                .isPhoneVerified(false)
                .authorities(new ArrayList<GrantedAuthority>(Collections.singletonList(new SimpleGrantedAuthority(ERole.ROLE_USER.name()))))
                .build();
        user = mongoTemplate.insert(user);
        userKafkaTemplate.send(userTopic.name(), registrationRequest.toMessage());
        return user;
    }
}
