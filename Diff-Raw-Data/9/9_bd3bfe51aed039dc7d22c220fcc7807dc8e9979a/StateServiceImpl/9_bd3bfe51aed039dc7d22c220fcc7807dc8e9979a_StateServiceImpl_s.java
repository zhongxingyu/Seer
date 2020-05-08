 package com.printo.services;
 
 import java.util.List;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 import com.printo.dao.StateDAO;
 import com.printo.model.State;
 
 /**
  * @author soft-koushik
  *
  */
 @Service
 public class StateServiceImpl implements StateService {
 
 	@Autowired
 	private StateDAO stateDAO;
 	
 	public void addState(State state) {
 		// TODO Auto-generated method stub
 		stateDAO.addState(state);
 	}
 
	
 	public List<State> listState() {
 		// TODO Auto-generated method stub
 		return stateDAO.listState();
 	}
 
	
 	public void removeState(Integer id) {
 		// TODO Auto-generated method stub
 		stateDAO.removeState(id);
 	}
 
 	
 	
 }
