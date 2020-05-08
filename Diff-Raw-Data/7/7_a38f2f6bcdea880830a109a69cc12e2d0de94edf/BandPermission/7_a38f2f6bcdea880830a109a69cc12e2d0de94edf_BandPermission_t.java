 package com.indicrowd.auth;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.core.Authentication;
 
 import com.indicrowd.post.Comment;
 import com.indicrowd.post.Post;
 import com.indicrowd.user.model.UserInfo;
 
 public class BandPermission implements Permission {
 
 	@Autowired
 	AuthService authService;
 	
 	@Override
 	public boolean isAllowed(Authentication authentication,
 			Object targetDomainObject) {
 		boolean hasPermission = false;
 
 		if (isAuthenticated(authentication) && isBandId(targetDomainObject)) {
 			if (authService.isAuthorizedUserOfBand((Long)targetDomainObject)) {
 				hasPermission = true;
 			}
 		}
 		
 		return hasPermission;
 	}
 
 	private boolean isBandId(Object targetDomainObject) {
         return targetDomainObject instanceof Long && (Long)targetDomainObject  > 0;
     }
 
 	
 	private boolean isAuthenticated(Authentication authentication) {
         return authentication != null && authentication.getPrincipal() instanceof UserInfo;
     }
 }
