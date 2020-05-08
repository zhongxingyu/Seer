 package com.masternaut.repository;
 
 import com.masternaut.CustomerIdentifiable;
 import com.masternaut.DomainHelper;
 import com.masternaut.PaddingtonException;
 import com.masternaut.domain.Customer;
 import com.masternaut.factory.CustomerMongoFactory;
 import com.masternaut.repository.system.CustomerRepository;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.data.domain.Page;
 import org.springframework.data.domain.PageImpl;
 import org.springframework.data.domain.PageRequest;
 import org.springframework.data.mongodb.core.MongoOperations;
 import org.springframework.data.mongodb.core.query.Criteria;
 import org.springframework.data.mongodb.core.query.Query;
 import org.springframework.data.mongodb.repository.query.QueryUtils;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 public class BaseCustomerRepository<T extends CustomerIdentifiable> {
 
     @Autowired
     private CustomerMongoFactory customerMongoFactory;
 
     @Autowired
     private CustomerRepository customerRepository;
 
     protected Class<T> clazz;
 
     public BaseCustomerRepository(Class<T> clazz) {
         this.clazz = clazz;
     }
 
     protected MongoOperations createMongoOperations(String customerId){
         return customerMongoFactory.create(customerId);
     }
 
     public void deleteAllForCustomer(String customerId) {
         MongoOperations mongoOperations = customerMongoFactory.create(customerId);
 
         Criteria criteria = createCriteriaForCustomer(customerId);
         Query query = Query.query(criteria);
 
         mongoOperations.remove(query, clazz);
     }
 
     public void deleteById(String id, String customerId) {
         MongoOperations mongoOperations = customerMongoFactory.create(customerId);
 
         Criteria criteria = createCriteriaForCustomer(customerId);
         criteria.and("id").is(id);
 
         mongoOperations.remove(new Query(criteria), clazz);
     }
 
     protected Criteria createCriteriaForCustomer(String customerId) {
         return Criteria.where("customerId").is(customerId);
     }
 
     public void bulkInsert(Iterable<T> tlist) {
         if (!tlist.iterator().hasNext()){
             return;
         }
 
         ArrayList<T> list = new ArrayList<T>();
 
         for (T t : tlist) {
             list.add(t);
         }
 
         String customerId = DomainHelper.getCustomerIdFromList(tlist);
 
         MongoOperations mongoTemplate = customerMongoFactory.create(customerId);
 
         mongoTemplate.insert(list, clazz);
     }
 
     public void save(T t) {
        save(Arrays.asList(t));
     }
 
     public void save(T... tlist) {
         save(Arrays.asList(tlist));
     }
 
     public void save(Iterable<T> tlist) {
         if (!tlist.iterator().hasNext()){
             return;
         }
 
         for(T t : tlist){
            if (t.getCustomerId() == null){
                throw new PaddingtonException(String.format("CustomerId not set on %s", clazz.getSimpleName()));
            }
        }

        for(T t : tlist){
             MongoOperations mongoTemplate = customerMongoFactory.create(t.getCustomerId());
             mongoTemplate.save(t);
         }
     }
 
     public T findById(String id, String customerId) {
         T t = tryFindById(id, customerId);
 
         if (t == null) {
             String error = String.format("%s with id of '%s' not found.", clazz.getSimpleName(), id);
             throw new PaddingtonException(error);
         }
 
         return t;
     }
 
     public T tryFindById(String id, String customerId) {
         MongoOperations customerMongoOperations = customerMongoFactory.create(customerId);
 
         Criteria criteria = createCriteriaForCustomer(customerId);
         criteria.and("id").is(id);
 
         return customerMongoOperations.findOne(new Query(criteria), clazz);
     }
 
     public List<T> findAllForCustomer(String customerId) {
         MongoOperations mongoTemplate = customerMongoFactory.create(customerId);
         Criteria criteria = createCriteriaForCustomer(customerId);
         Query query = Query.query(criteria);
 
         List<T> entities = mongoTemplate.find(query, clazz);
 
         setCustomerIdOnList(customerId, entities);
 
         return entities;
     }
 
     public Page<T> findAllForCustomer(String customerId, PageRequest pageable) {
         MongoOperations mongoTemplate = customerMongoFactory.create(customerId);
         Criteria criteria = createCriteriaForCustomer(customerId);
         Query query = Query.query(criteria);
 
         Long count = countForCustomerId(customerId);
 
         List<T> list = mongoTemplate.find(QueryUtils.applyPagination(query, pageable), clazz);
 
         setCustomerIdOnList(customerId, list);
 
         return new PageImpl<T>(list, pageable, count);
     }
 
     private void setCustomerIdOnList(String customerId, Iterable<T> list) {
         for (T t : list) {
             t.setCustomerId(customerId);
         }
     }
 
     public long countForAllCustomers() {
         List<Customer> allCustomers = customerRepository.findAll();
 
         int count = 0;
         for (Customer customer : allCustomers) {
             MongoOperations customerMongoTemplate = customerMongoFactory.create(customer.getId());
 
             Criteria criteria = createCriteriaForCustomer(customer.getId());
 
             count += customerMongoTemplate.count(new Query(criteria), clazz);
         }
 
         return count;
     }
 
     public long countForCustomerId(String customerId) {
         MongoOperations mongoTemplate = customerMongoFactory.create(customerId);
 
         Criteria criteria = createCriteriaForCustomer(customerId);
 
         return mongoTemplate.count(new Query(criteria), clazz);
     }
 
     public List<T> findByIds(Iterable<String> ids, String customerId) {
         Criteria criteria = createCriteriaForCustomer(customerId);
 
         List<String> idsList = iterableToList(ids);
 
         criteria.and("id").in(idsList);
 
         MongoOperations mongoOperations = createMongoOperations(customerId);
 
         return mongoOperations.find(new Query(criteria), clazz);
     }
 
     private <T> List<T> iterableToList(Iterable<T> tIterable) {
         List<T> list = new ArrayList<T>();
 
         for(T t : tIterable){
             list.add(t);
         }
 
         return list;
     }
 
     public boolean exists(String id, String customerId) {
         T t = tryFindById(id, customerId);
 
         return t != null;
     }
 }
