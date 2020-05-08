 package com.presupuestar.user.service;
 
 import com.presupuestar.model.user.Commerce;
import com.presupuestar.model.user.User;
 
 public interface CommerceService {
 
 	Commerce registerNewCommerceAccount(String email, String password,
 			String reason);
 
	User userLogin(String email, String password);
 
 }
