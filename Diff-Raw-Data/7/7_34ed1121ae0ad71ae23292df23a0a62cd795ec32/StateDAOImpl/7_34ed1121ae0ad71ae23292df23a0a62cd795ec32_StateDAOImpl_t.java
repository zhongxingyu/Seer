 package com.printo.dao;
 
 import java.util.List;
 
 import org.hibernate.SessionFactory;
 import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
 
 import com.printo.model.State;
 
 /**
  * @author soft-koushik
  *
  */
 @Repository
@Transactional
 public class StateDAOImpl implements StateDAO {
 	
 	
 	@Autowired
	@Qualifier("sessionFactory")
 	private SessionFactory sessionFactory;
 	
 	public void addState(State state) {
 		// TODO Auto-generated method stub
 		sessionFactory.getCurrentSession().save(state);
 	}
 	
 	public List<State> listState(){
 		 return sessionFactory.getCurrentSession().createQuery("from State").list();
 	}
 	
 	public void removeState(Integer id){
 		State state = (State) sessionFactory.getCurrentSession().load(State.class, id);
         if (null != state) {
             sessionFactory.getCurrentSession().delete(state);
         }
  
 	}
 
 	
 
 }
