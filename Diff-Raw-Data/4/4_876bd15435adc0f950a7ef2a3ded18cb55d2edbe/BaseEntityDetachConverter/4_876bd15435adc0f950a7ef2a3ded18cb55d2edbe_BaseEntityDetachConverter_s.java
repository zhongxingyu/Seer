 package oshop.web.converter;
 
 import oshop.model.BaseEntity;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 public abstract class BaseEntityDetachConverter<T extends BaseEntity<ID>, ID extends Serializable>
         implements EntityDetachConverter<T, ID> {
 
     protected abstract Class<T> entityClass();
     protected abstract void detach(T entity, T detachedEntity) throws Exception;
 
     @Override
     public T detach(T entity)  throws Exception {
         T detachedEntity = entityClass().newInstance();
         detachedEntity.setId(entity.getId());
         detachedEntity.setLastUpdate(entity.getLastUpdate());
         detachedEntity.setVersion(entity.getVersion());
 
         detach(entity, detachedEntity);
 
         return detachedEntity;
     }
 
     @Override
     public List<T> detach(List<T> entities)  throws Exception {
         if (entities == null) {
             return null;
         }
 
         List<T> detachedList = new ArrayList<T>(entities.size());
 
         for (T entity: entities) {
             detachedList.add(detach(entity));
         }
 
         return detachedList;
     }
 }
