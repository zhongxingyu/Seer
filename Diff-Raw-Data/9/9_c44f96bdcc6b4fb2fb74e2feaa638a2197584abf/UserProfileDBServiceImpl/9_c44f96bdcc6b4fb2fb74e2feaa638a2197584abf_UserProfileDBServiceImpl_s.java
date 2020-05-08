 package com.bd.mybatis.dbservice.impl;
 
 import java.util.List;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 import com.bd.mybatis.bean.UserProfile;
 import com.bd.mybatis.dao.UserProfileDao;
 import com.bd.mybatis.dbservice.UserProfileDBService;
 
@Service
 public class UserProfileDBServiceImpl implements UserProfileDBService {
 
 	@Autowired
 	private UserProfileDao userProfileDao;
 
 	@Override
 	public List<UserProfile> selectAllUserProfile() throws Exception {
 		return userProfileDao.selectAllUserProfile();
 	}
 
 	@Override
 	public UserProfile selectUserProfileByID(long profileID) throws Exception {
 		return userProfileDao.selectUserProfileByID(profileID);
 	}
 
 	@Override
 	public int createUserProfile(UserProfile userProfile) throws Exception {
 		return userProfileDao.createUserProfile(userProfile);
 	}
 
 	@Override
 	public int updateUserProfile(UserProfile userProfile) throws Exception {
 		return userProfileDao.updateUserProfile(userProfile);
 	}
 
 	@Override
 	public int deleteUserProfile(long profileID) throws Exception {
 		return userProfileDao.deleteUserProfile(profileID);
 	}
 
 	@Override
 	public int getMaxUserProfileID() throws Exception {
 		return userProfileDao.selectMaxUserProfileID();
 	}
 
 }
