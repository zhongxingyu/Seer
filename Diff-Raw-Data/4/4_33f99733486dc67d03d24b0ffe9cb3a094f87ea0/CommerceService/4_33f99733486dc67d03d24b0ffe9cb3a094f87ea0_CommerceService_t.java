 package com.presupuestar.user.service;
 
 import com.presupuestar.model.user.Commerce;
 
 public interface CommerceService {
 
 	Commerce registerNewCommerceAccount(String email, String password,
 			String reason);
 
	Commerce userLogin(String email, String password);
 
 }
