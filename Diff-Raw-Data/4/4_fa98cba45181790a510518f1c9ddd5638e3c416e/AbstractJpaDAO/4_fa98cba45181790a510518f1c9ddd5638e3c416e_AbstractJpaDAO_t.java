 package org.camelapp.dao;
 
 import org.springframework.beans.factory.annotation.Autowired;
 
 import javax.persistence.*;
 import java.util.List;
 
 public abstract class AbstractJpaDAO<T> {
 
     private Class<T> clazz;
 
     @PersistenceContext
     private EntityManager entityManager;
 
     public void setClazz(Class<T> clazzToSet){
         this.clazz = clazzToSet;
     }
 
 //    public T findOne(Long id){
 //        return this.entityManager.find(this.clazz, id);
 //    }
 //    public List<T> findAll(){
 //        return this.entityManager.createQuery("from " + this.clazz.getName()).getResultList();
 //    }
 
 
     public void save(T entity) {
         entityManager.persist(entity);
     }
 
    public List<T> findAll() {
         return entityManager.createQuery("from " + this.clazz.getName()).getResultList();
     }
 
 //    public void update(T entity){
 //        this.entityManager.merge(entity);
 //    }
 //
 //    public void delete(T entity){
 //        this.entityManager.remove(entity);
 //    }
 //    public void deleteById(Long entityId){
 //        T entity = this.findOne(entityId);
 //
 //        this.delete(entity);
 //    }
 }
