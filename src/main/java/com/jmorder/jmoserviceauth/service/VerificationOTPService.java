package com.jmorder.jmoserviceauth.service;

import com.jmorder.jmoserviceauth.model.User;
import com.jmorder.jmoserviceauth.model.VerificationOTP;
import com.jmorder.jmoserviceauth.service.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class VerificationOTPService {
    private static final Class<VerificationOTP> ENTITY_CLASS = VerificationOTP.class;
    @Autowired
    UserService userService;

    @Autowired
    private MongoTemplate mongoTemplate;

    public boolean isOTPMatch(String id, String otp) {
        return mongoTemplate.exists(Query.query(Criteria.where("id").is(id).and("otp").is(otp)), ENTITY_CLASS);
    }

    public VerificationOTP loadVerificationOTPById(String id) {
        return mongoTemplate.findById(id, ENTITY_CLASS);
    }

    public VerificationOTP loadAndDeleteOTPById(String id) {
        VerificationOTP verificationOTP = mongoTemplate.findAndRemove(Query.query(Criteria.where("id").is(id)), ENTITY_CLASS);
        if (verificationOTP == null) throw new ResourceNotFoundException();
        return verificationOTP;
    }

    public VerificationOTP createVerificationOTP(String phone) {
        String linkableUserId;
        try {
            User linkableUser = userService.loadUserByUsername(phone);
            linkableUserId = linkableUser.getId();
        } catch (UsernameNotFoundException e) {
            linkableUserId = null;
        }
        VerificationOTP verificationOTP = new VerificationOTP(phone, linkableUserId);
        return mongoTemplate.insert(verificationOTP);
    }
}
