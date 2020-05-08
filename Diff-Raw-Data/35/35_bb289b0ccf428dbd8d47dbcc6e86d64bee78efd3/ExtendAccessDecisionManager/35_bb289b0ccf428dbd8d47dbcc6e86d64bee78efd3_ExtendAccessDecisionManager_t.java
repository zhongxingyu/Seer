 /**
  * @author hyd 2012-11-30 上午9:09:50 
  * @version 1.0
  */
 package f.rd.paths.web.security.extend.impl;
 
 import java.util.Collection;
 import java.util.Iterator;
 
 import org.springframework.security.access.AccessDecisionManager;
 import org.springframework.security.access.AccessDeniedException;
 import org.springframework.security.access.ConfigAttribute;
 import org.springframework.security.access.SecurityConfig;
 import org.springframework.security.authentication.InsufficientAuthenticationException;
 import org.springframework.security.core.Authentication;
 import org.springframework.security.core.GrantedAuthority;
 
 /** 
  * 匹配用户拥有权限和请求权限
  * <p>
  *  
  */
 public class ExtendAccessDecisionManager implements AccessDecisionManager {
 
 	// In this method, need to compare authentication with configAttributes.
 	// 1, A object is a URL, a filter was find permission configuration by this URL, and pass to here.
 	// 2, Check authentication has attribute in permission configuration(configAttributes)
 	// 3, If not match corresponding authentication, throw a AccessDeniedException.
 	public void decide(Authentication authentication, Object object, Collection<ConfigAttribute> configAttributes)
 			throws AccessDeniedException, InsufficientAuthenticationException {
 		if (configAttributes == null) {
 			return;
 		}
 		Iterator<ConfigAttribute> ite = configAttributes.iterator();
 		while (ite.hasNext()) {
 			ConfigAttribute ca = ite.next();
 			String needRole = ((SecurityConfig) ca).getAttribute();
 			for (GrantedAuthority ga : authentication.getAuthorities()) {
 				if (needRole.equals(ga.getAuthority())) { // ga is user's role.
 					return;
 				}
 			}
 		}
 		throw new AccessDeniedException("no right");
 	}
 
 	public boolean supports(ConfigAttribute attribute) {
 		return true;
 	}
 
 	public boolean supports(Class<?> clazz) {
 		return true;
 	}
 
 }
