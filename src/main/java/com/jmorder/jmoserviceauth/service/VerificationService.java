package com.jmorder.jmoserviceauth.service;

import com.jmorder.jmoserviceauth.model.User;
import com.jmorder.jmoserviceauth.model.OnetimePassword;
import com.jmorder.jmoserviceauth.service.exceptions.ResourceNotFoundException;
import com.jmorder.jmoserviceauth.util.FirebaseDynamicLink;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class VerificationService {
    private static final Class<OnetimePassword> ENTITY_CLASS = OnetimePassword.class;
    @Autowired
    UserService userService;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    FirebaseDynamicLink firebaseDynamicLink;

    public boolean isOTPMatch(String id, String otp) {
        return mongoTemplate.exists(Query.query(Criteria.where("id").is(id).and("otp").is(otp)), ENTITY_CLASS);
    }

    public OnetimePassword loadVerificationOTPById(String id) {
        return mongoTemplate.findById(id, ENTITY_CLASS);
    }

    public OnetimePassword loadAndDeleteOTPById(String id) {
        OnetimePassword onetimePassword = mongoTemplate.findAndRemove(Query.query(Criteria.where("id").is(id)), ENTITY_CLASS);
        if (onetimePassword == null) throw new ResourceNotFoundException();
        return onetimePassword;
    }

    public OnetimePassword createVerificationOTP(String phone) {
        log.info(phone);
        String linkableUserId;
        try {
            User linkableUser = userService.loadUserByUsername(phone);
            linkableUserId = linkableUser.getId();
        } catch (UsernameNotFoundException e) {
            linkableUserId = null;
        }
        OnetimePassword onetimePassword = new OnetimePassword(phone, linkableUserId);
        return mongoTemplate.insert(onetimePassword);
    }
}
