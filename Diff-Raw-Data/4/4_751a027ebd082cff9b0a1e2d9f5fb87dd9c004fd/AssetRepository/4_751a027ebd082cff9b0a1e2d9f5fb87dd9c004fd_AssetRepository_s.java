 package com.masternaut.repository;
 
 import com.masternaut.domain.Asset;
import com.masternaut.domain.Customer;
 import org.springframework.data.mongodb.core.MongoTemplate;
 import org.springframework.data.mongodb.core.query.Query;
 
 public class AssetRepository {
 
     private RepositoryFactory repositoryFactory;
 
     public AssetRepository(RepositoryFactory repositoryFactory) {
         this.repositoryFactory = repositoryFactory;
     }
 
     public void deleteAll(String customerId) {
         MongoTemplate mongoTemplate = repositoryFactory.createMongoTemplateForCustomerId(customerId);
 
        mongoTemplate.remove(new Query(), Customer.class);
     }
 
     public void save(Asset asset) {
         MongoTemplate mongoTemplate = repositoryFactory.createMongoTemplateForCustomerId(asset.getCustomerId());
 
         mongoTemplate.save(asset);
     }
 
     public Asset findById(String id, String customerId) {
         MongoTemplate mongoTemplate = repositoryFactory.createMongoTemplateForCustomerId(customerId);
 
         return mongoTemplate.findById(id, Asset.class);
     }
 }
