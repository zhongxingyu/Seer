 package com.jdc.themis.dealer.service.impl;
 
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 import javax.time.Instant;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 import com.google.common.base.Preconditions;
 import com.jdc.themis.dealer.data.dao.UserDAO;
 import com.jdc.themis.dealer.domain.UserInfo;
 import com.jdc.themis.dealer.domain.UserRole;
 import com.jdc.themis.dealer.service.RefDataQueryService;
 import com.jdc.themis.dealer.service.UserService;
 import com.jdc.themis.dealer.utils.Performance;
 import com.jdc.themis.dealer.web.domain.AddNewUserRequest;
 import com.jdc.themis.dealer.web.domain.GetUserInfoResponse;
 import com.jdc.themis.dealer.web.domain.GetUserRoleResponse;
 import com.jdc.themis.dealer.web.domain.ModifyUserRequest;
 import com.jdc.themis.dealer.web.domain.ResetPasswordRequest;
 import com.jdc.themis.dealer.web.domain.UserRoleDetail;
 
 /**
  * Service layer to manage user information.
  * 
  * @author Kai Chen
  *
  */
 @Service
 public class UserServiceImpl implements UserService {
 	
 	@Autowired
 	private UserDAO userDAL;
 	public void setUserDAL(UserDAO userDAL) {
 		this.userDAL = userDAL;
 	}
 
 	@Autowired
 	private RefDataQueryService refDataQueryDAL;
 
 	public void setRefDataQueryDAL(RefDataQueryService refDataQueryDAL) {
 		this.refDataQueryDAL = refDataQueryDAL;
 	}
 
 	@Override
 	@Performance
 	public Instant addNewUser(final AddNewUserRequest request) {
 		Preconditions.checkNotNull(request.getUsername(), "user name can't be null");
 		Preconditions.checkArgument(userDAL.getUser(request.getUsername()).isNone(), "user name already exists");
 		Preconditions.checkNotNull(request.getPassword(), "password can't be null");
 		Preconditions.checkNotNull(request.getUserRole(), "user role can't be null");
 		
 		final UserInfo user = new UserInfo();
 		user.setUsername(request.getUsername());
         user.setPassword(hashPassword(request.getPassword()));
 		user.setUserRoleID(request.getUserRole());
 		user.setActive(Boolean.TRUE);
 		if ( request.getUpdatedBy() == null ) {
 			user.setUpdatedBy(request.getUsername());
 		} else {
 			user.setUpdatedBy(request.getUpdatedBy());
 		}
 		if ( request.getDealerID() != null ) {
 			user.setDealerID(refDataQueryDAL.getDealer(request.getDealerID()).getId());
 		} 
 		user.setActive(Boolean.TRUE);
 		return userDAL.saveOrUpdateUser(user);
 	}
 
 	@Override
 	@Performance
 	public Instant disableUser(final String username) {
 		Preconditions.checkNotNull(username, "username can't be null");
 		Preconditions.checkArgument(userDAL.getUser(username).isSome(), "unknown user name");
 
 		final UserInfo user = userDAL.getUser(username).some();
 		user.setActive(Boolean.FALSE);
 		return userDAL.saveOrUpdateUser(user);
 	}
 
 	@Override
 	@Performance
 	public GetUserInfoResponse getUser(final String username) {
 		Preconditions.checkNotNull(username, "username can't be null");
 		Preconditions.checkArgument(userDAL.getUser(username).isSome(), "unknown user name");
 
 		final UserInfo user = userDAL.getUser(username).some();
 		final GetUserInfoResponse response = new GetUserInfoResponse();
 		response.setUsername(user.getUsername());
 		if ( user.getDealerID() != null ) {
 			response.setDealer(refDataQueryDAL.getDealer(user.getDealerID()));
 		} 
 		response.setActive(user.getActive());
 		response.setRole(userDAL.getUserRole(user.getUserRoleID()).some().getName());
 		return response;
 	}
 
 	@Override
 	@Performance
 	public boolean authVerity(final String username, final String password) {
 	    Preconditions.checkNotNull(username, "username can't be null");
 	    Preconditions.checkNotNull(password, "password can't be null");
 	    Preconditions.checkArgument(userDAL.getUser(username).isSome(), "unknown user name");
 
 	    final UserInfo user = userDAL.getUser(username).some();
 	    
 	    return user.getPassword().equals(hashPassword(password));
 	}
 	   
 	@Override
 	public Instant enableUser(final String username) {
 		Preconditions.checkNotNull(username, "username can't be null");
 		Preconditions.checkNotNull(userDAL.getUser(username).isSome(), "unknown user name");
 
 		final UserInfo user = userDAL.getUser(username).some();
 		user.setActive(Boolean.TRUE);
 		return userDAL.saveOrUpdateUser(user);
 	}
 
 	@Override
 	public Instant resetPassword(final ResetPasswordRequest request) {
 		Preconditions.checkNotNull(request.getUsername(), "user name can't be null");
 		Preconditions.checkNotNull(request.getOldPassword(), "old password can't be null");
 		Preconditions.checkNotNull(request.getNewPassword(), "new password can't be null");
 		Preconditions.checkArgument(userDAL.getUser(request.getUsername()).isSome(), "user name does not exists");
 		Preconditions.checkArgument(!request.getNewPassword().equals(request.getOldPassword()), 
 				"old password equals to the new password");
 		Preconditions.checkArgument(hashPassword(request.getOldPassword()).equals(userDAL.getUser(request.getUsername()).some().getPassword()), 
 				"password doesn't match");
 		
 		final UserInfo user = userDAL.getUser(request.getUsername()).some();
 		user.setPassword(hashPassword(request.getNewPassword()));
 		return userDAL.saveOrUpdateUser(user);
 	}
 
 	@Override
 	public Instant modifyUser(final ModifyUserRequest request) {
 		Preconditions.checkNotNull(request.getUsername(), "user name can't be null");
 		Preconditions.checkNotNull(request.getPassword(), "password can't be null");
 		Preconditions.checkNotNull(request.getUpdatedBy(), "updated by can't be null");
 		Preconditions.checkArgument(userDAL.getUser(request.getUsername()).isSome(), "user name does not exists");
 		Preconditions.checkArgument(hashPassword(request.getPassword()).equals(userDAL.getUser(request.getUsername()).some().getPassword()), 
 				"password doesn't match");
 		
 		final UserInfo user = userDAL.getUser(request.getUsername()).some();
 		user.setDealerID(request.getDealerID());
 		if ( request.getUserRole() != null ) {
 			user.setUserRoleID(request.getUserRole());
 		} 
 		user.setUpdatedBy(request.getUpdatedBy());
 		return userDAL.saveOrUpdateUser(user);
 	}
 
 	@Override
 	public GetUserInfoResponse getCurrentUser(HttpServletRequest req) {
 	    HttpSession session = req.getSession(false);
 	    String userAlias = (null == session) ? null : (String) session.getAttribute("userAlias");
 
 	    return (null == userAlias) ? null : getUser(userAlias);
 	}
 	
 	public GetUserRoleResponse getUserRoles() {
 		final GetUserRoleResponse response = new GetUserRoleResponse();
 		for ( final UserRole role : userDAL.getUserRoles() ) {
 			final UserRoleDetail roleDetail = new UserRoleDetail();
 			roleDetail.setId(role.getId());
 			roleDetail.setName(role.getName());
 			response.getDetail().add(roleDetail);
 		}
 		return response;
 	}
 
     private String hashPassword(String password) {
       byte[] passHash = null;
       try {
         MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
         passHash = sha256.digest(password.getBytes());
       } catch (NoSuchAlgorithmException e) {
         throw new RuntimeException(e);
       }        
      return new String(passHash);
     }
 
 }
