 package com.bd.mybatis.dbservice;
 
 import java.util.List;
 
 import com.bd.mybatis.bean.UserProfile;
 
 public interface UserProfileDBService {
 
 	public List<UserProfile> selectAllUserProfile() throws Exception;
 
 	public UserProfile selectUserProfileByID(long profileID) throws Exception;
 
 	public int createUserProfile(UserProfile userProfile) throws Exception;
 
 	public int updateUserProfile(UserProfile userProfile) throws Exception;
 
 	public int deleteUserProfile(long profileID) throws Exception;
	
 	public int getMaxUserProfileID() throws Exception;
 
 }
