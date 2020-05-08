 package hu.meza;
 
 public interface Action {
 	void setAuthenticationData(Object data);
 
 	void execute();
 
	<T> T requestData();
 
	<T> T responseData();
 }
