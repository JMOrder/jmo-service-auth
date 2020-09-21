package com.jmorder.jmoserviceauth.service;

import com.jmorder.jmoserviceauth.model.RefreshToken;
import com.mongodb.client.result.DeleteResult;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

@Service
public class RefreshTokenService {
  private static final Class<RefreshToken> ENTITY_CLASS = RefreshToken.class;
  @Autowired
  private MongoTemplate mongoTemplate;

  public RefreshToken findById(String id) {
    return mongoTemplate.findById(id, ENTITY_CLASS);
  }

  public RefreshToken createRefreshTokenByUsername(String username) {
    return mongoTemplate.insert(new RefreshToken(username));
  }

  public DeleteResult removeById(String id) {
    return mongoTemplate.remove(Query.query(Criteria.where("id").is(id)), ENTITY_CLASS);
  }
}
