 package hu.meza;
 
 public interface Action {
 	void setAuthenticationData(Object data);
 
 	void execute();
 
	Object requestData();
 
	Object responseData();
 }
