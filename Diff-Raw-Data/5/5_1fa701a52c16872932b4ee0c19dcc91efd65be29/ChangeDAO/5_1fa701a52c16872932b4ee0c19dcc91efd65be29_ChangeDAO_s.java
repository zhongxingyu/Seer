 package com.myproject.bookexchange.dao.impl;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 import org.springframework.data.mongodb.core.query.Criteria;
 import org.springframework.data.mongodb.core.query.Query;
 
 import com.myproject.bookexchange.dao.IChangeDAO;
 import com.myproject.bookexchange.domain.BookVO;
 import com.myproject.bookexchange.domain.ChangeVO;
 import com.myproject.bookexchange.domain.UserVO;
 
 public class ChangeDAO extends GenericMongoDAO<ChangeVO> implements IChangeDAO {
 
   public ChangeDAO() {
     super(ChangeVO.class);
   }
 
   @Override
   public void beforeEntityUpdateAdd(ChangeVO entity) {
     // TODO Auto-generated method stub
     
   }
 
   @Override
   public void beforeEntityDelete(ChangeVO entity) {
     // TODO Auto-generated method stub
     
   }
 
   @Override
   public List<ChangeVO> getChangesByBook(BookVO book) {
     Query q = new Query(Criteria.where("book").is(book.getId()));
     return mongo.find(q, ChangeVO.class);
   }
 
   @Override
   public List<ChangeVO> getChangesByReceiver(UserVO receiver) {
     Query q = new Query(Criteria.where("receiver").is(receiver.getId()));
     return mongo.find(q, ChangeVO.class);
   }
 
   @Override
   public List<ChangeVO> getChangesByGiver(UserVO giver) {
     Query q = new Query(Criteria.where("sender").is(giver.getId()));
     return mongo.find(q, ChangeVO.class);
   }
 
   @Override
   public List<ChangeVO> getAllChangesByUser(UserVO user) {
    Query q = new Query(Criteria.where("sender").is(user.getId()).orOperator(Criteria.where("receiver").is(user.getId())));
     return mongo.find(q, ChangeVO.class);
   }
 
   @Override
   public List<ChangeVO> getAllChangesForDate(Date date) {
     Calendar cal = Calendar.getInstance();
     cal.setTime(date);
     cal.set(Calendar.HOUR_OF_DAY, 0);
     cal.set(Calendar.MINUTE, 0);
     cal.set(Calendar.SECOND, 0);
     Date from = cal.getTime();
     cal.add(Calendar.DAY_OF_MONTH, 1);
     Date to = cal.getTime();
    Query q = new Query(Criteria.where("date").gte(from).andOperator(Criteria.where("date").lt(to)));
     return mongo.find(q, ChangeVO.class);
   }
 
 }
