 package cn.mobiledaily.repository;
 
 import cn.mobiledaily.domain.Exhibition;
 import cn.mobiledaily.domain.mobile.Attendee;
 import cn.mobiledaily.domain.mobile.ExhibitionContent;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.data.domain.Sort;
 import org.springframework.data.mongodb.core.MongoOperations;
 import org.springframework.data.mongodb.core.query.Criteria;
 import org.springframework.data.mongodb.core.query.Query;
 import org.springframework.stereotype.Repository;
 import org.springframework.transaction.annotation.Transactional;
 
 import java.util.ArrayList;
 import java.util.List;
 
 @Repository
 @Transactional(readOnly = true)
 public class MongoExhibitionRepository implements ExhibitionRepository {
     @Autowired
     private MongoOperations mongoOperations;
 
     @Override
     @Transactional
     public void save(Exhibition exhibition) {
         mongoOperations.save(exhibition);
     }
 
     @Override
     @Transactional
     public void save(ExhibitionContent content) {
         mongoOperations.save(content, content.getExhibition().getCode() + content.getCollectionSuffix());
     }
 
     @Override
     public Exhibition findByCode(String code) {
         return mongoOperations.findOne(Query.query(Criteria.where("code").is(code)), Exhibition.class);
     }
 
     @Override
     public List<Exhibition> findAll() {
         return mongoOperations.find(new Query().with(new Sort(Sort.Direction.DESC, "createdAt")), Exhibition.class);
 //        return mongoOperations.findAll(Exhibition.class);
     }
 
     @Override
     public Exhibition findById(String id) {
         return mongoOperations.findById(id, Exhibition.class);
     }
 
     @Override
     @Transactional
     public void delete(Exhibition exhibition) {
         mongoOperations.remove(exhibition);
     }
 
     @Override
     @Transactional
     public void delete(ExhibitionContent content) {
         mongoOperations.remove(content, content.getExhibition().getCode() + content.getCollectionSuffix());
     }
 
     @Override
     public <T extends ExhibitionContent> List<T> findContents(Exhibition exhibition, Class<T> type, Sort sort) {
         try {
             List<T> list;
             if (sort != null) {
                 list = mongoOperations.find(new Query().with(sort),
                         type, exhibition.getCode() + type.newInstance().getCollectionSuffix());
             } else {
                 list = mongoOperations.findAll(type, exhibition.getCode() + type.newInstance().getCollectionSuffix());
             }
             for (T t : list) {
                 t.setExhibition(exhibition);
             }
             return list;
         } catch (Exception e) {
             return new ArrayList<>();
         }
     }
 
     @Override
     public <T extends ExhibitionContent> List<T> findContents(Exhibition exhibition, Class<T> type) {
         return findContents(exhibition, type, null);
     }
 
     @Override
     public <T extends ExhibitionContent> T findContentById(Exhibition exhibition, String id, Class<T> type) {
         try {
             T t = mongoOperations.findById(id, type, exhibition.getCode() + type.newInstance().getCollectionSuffix());
             t.setExhibition(exhibition);
             return t;
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
     }
 
     @Override
     public List<Attendee> findAttendeesByServiceToken(Exhibition exhibition, String token) {
        List<Attendee> list = mongoOperations.find(Query.query(Criteria.where("serviceToken").is(token)),
                 Attendee.class,
                 exhibition.getCode() + new Attendee().getCollectionSuffix());
        for (Attendee attendee : list) {
            attendee.setExhibition(exhibition);
        }
        return list;
     }
 }
