 package reevent.dao;
 
 import reevent.domain.EntityBase;
 
 import java.util.List;
 import java.util.UUID;
 
 public interface EntityDao<T extends EntityBase> {
     T load(UUID id);
     T get(UUID id);
    T update(T entity);
    T save(T entity);
     void delete(UUID id);
     List<T> findAll(int first, int max);
 }
